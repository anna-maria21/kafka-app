package com.example.kafka.kafka;

import com.example.kafka.dto.OperType;
import com.example.kafka.entity.Account;
import com.example.kafka.entity.Operation;
import com.example.kafka.exception.NoSuchAccountException;
import com.example.kafka.exception.NoSuchOperationException;
import com.example.kafka.repository.jpa.AccountRepo;
import com.example.kafka.repository.jpa.OperationRepo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@AllArgsConstructor
public class MyStreamsProcessing {

    static final String HASH_KEY = "Operation";

    private final AccountRepo accountRepo;
    private final OperationRepo operationRepo;
    private final RedisTemplate<String, Object> redisTemplate;

//    @Autowired
    public void process(StreamsBuilder builder) {

        KStream<Long, Operation> operStream = builder
                .stream("change-balance", Consumed.with(Serdes.Long(), new JsonSerde<>(Operation.class)))
                .mapValues(operationRepo::save)
                .peek((key, value) -> {
                    log.info("Consumed from topic \"change-balance\": account - {}, operation - {}", key, value);
                    redisTemplate.opsForHash().put(HASH_KEY, key.toString(), value);
                    redisTemplate.expire(String.valueOf(key), 7, TimeUnit.DAYS);
                })
                .selectKey((key, value) -> value.getId());

        KStream<Long, Operation> confirmationStream = builder
                .stream("payment-confirmation", Consumed.with(Serdes.Long(), new JsonSerde<>(Operation.class)))
                .peek((key, value) -> log.info("Consumed from topic \"payment-confirmation\":  Confirmed operation id: {}", key));

        KStream<Long, Operation> confirmedOperations = operStream
                .join(
                    confirmationStream,
                    (operation, confirmed) -> {
                        Operation redisOperation = (Operation) redisTemplate.opsForHash().get(HASH_KEY, confirmed.getId().toString());
                        if (redisOperation != null) {
                            redisTemplate.opsForHash().delete(HASH_KEY, confirmed.getId().toString());
                            return redisOperation;
                        }
                        return operation;
                    },
                    JoinWindows.ofTimeDifferenceWithNoGrace(Duration.ofDays(7)),
                    StreamJoined.with(Serdes.Long(), new JsonSerde<>(Operation.class), new JsonSerde<>(Operation.class)))
                .peek((key, value) -> {
                    Operation operation = operationRepo.findById(key).orElseThrow(() -> new NoSuchOperationException(key));
                    operation.setIsConfirmed(true);
                    operationRepo.save(operation);
                });

        KStream<Long, Operation> refundOperations = confirmedOperations
                .filter((key, value) -> value != null && value.getOperationType() == OperType.REFUND)
                .selectKey((key, value) -> value.getAccountId())
                .peek((key, value) -> {
                    Account account = accountRepo.findById(value.getAccountId()).orElseThrow(() -> new NoSuchAccountException(value.getAccountId()));
                    BigDecimal newBalance = account.getBalance().add(value.getAmount());
                    setNewBalanceToAccount(account, newBalance);
                });
        refundOperations.to("dlg-succeed", Produced.with(Serdes.Long(), new JsonSerde<>(Operation.class)));

        KStream<Long, BigDecimal> withdrawalOperations = confirmedOperations
                .filter((key, value) -> value.getOperationType() == OperType.WITHDRAWAL)
                .selectKey((key, value) -> value.getId())
                .mapValues((key, value) -> {
                        Account account = accountRepo.findById(value.getAccountId()).orElseThrow(() -> new NoSuchAccountException(value.getAccountId()));
                        return account.getBalance().subtract(value.getAmount());
                });

        KStream<Long, Operation> withdrawalOperationsSuccess = withdrawalOperations
                .filter((key, value) -> value.compareTo(BigDecimal.ZERO) > 0)
                .mapValues((key, value) -> operationRepo.findById(key).orElseThrow(() -> new NoSuchOperationException(key)))
                .selectKey((key, value) -> value.getAccountId())
                .peek((key, value) -> {
                    Account account = accountRepo.findById(value.getAccountId()).orElseThrow(() -> new NoSuchAccountException(value.getAccountId()));
                    BigDecimal newBalance = account.getBalance().subtract(value.getAmount());
                    setNewBalanceToAccount(account, newBalance);
                });
        withdrawalOperationsSuccess.to("dlg-succeed", Produced.with(Serdes.Long(), new JsonSerde<>(Operation.class)));

        KStream<Long, Operation> withdrawalOperationsFail = withdrawalOperations
                .filter((key, value) -> value.compareTo(BigDecimal.ZERO) < 0)
                .mapValues((key, value) -> operationRepo.findById(key).orElseThrow(() -> new NoSuchOperationException(key)))
                .selectKey((key, value) -> value.getAccountId());
        withdrawalOperationsFail.to("dlg-failed", Produced.with(Serdes.Long(), new JsonSerde<>(Operation.class)));
    }

    private void setNewBalanceToAccount(Account account, BigDecimal newBalance) {
        account.setBalance(newBalance);
        accountRepo.save(account);
    }
}
