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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@AllArgsConstructor
public class MyStreamsProcessing {

    public static final String HASH_KEY = "Operation";
    public static final String DLG_SUCCEED = "dlg-succeed";
    public static final String CONFIRMATION = "payment-confirmation";
    public static final String CHANGE_BALANCE_TOPIC = "change-balance";
    public static final String RETRY_TOPIC = "my-retry";
    public static final String DLG_FAILED = "dlg-failed";

    private final AccountRepo accountRepo;
    private final OperationRepo operationRepo;
    private final RedisTemplate<String, Object> redisTemplate;
    private KafkaTemplate<Long, Operation> kafkaTemplate;

//    @Autowired
    public void process(StreamsBuilder builder) {

        KStream<Long, Operation> operStream = builder
                .stream(CHANGE_BALANCE_TOPIC, Consumed.with(Serdes.Long(), new JsonSerde<>(Operation.class)))
                .mapValues(operationRepo::save)
                .filter((key, value) -> {
                    try {
                        log.info("Consumed from topic {}: account - {}, operation - {}",CHANGE_BALANCE_TOPIC, key, value);
                        redisTemplate.opsForHash().put(HASH_KEY, key.toString(), value);
                        redisTemplate.expire(String.valueOf(key), 7, TimeUnit.DAYS);
                        return true;
                    } catch (Exception e) {
                        kafkaTemplate.send(RETRY_TOPIC, value);
                        return false;
                    }
                })
                .selectKey((key, value) -> value.getId());

        KStream<Long, Operation> confirmationStream = builder
                .stream(CONFIRMATION, Consumed.with(Serdes.Long(), new JsonSerde<>(Operation.class)))
                .peek((key, value) -> log.info("Consumed from topic {}:  Confirmed operation id: {}", CONFIRMATION, key));

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
                .filter((key, value) -> {
                    try {
                        Operation operation = operationRepo.findById(key).orElseThrow(() -> new NoSuchOperationException(key));
                        operation.setIsConfirmed(true);
                        operationRepo.save(operation);
                        return true;
                    } catch (Exception e) {
                        kafkaTemplate.send(RETRY_TOPIC, value);
                        return false;
                    }
                });

        KStream<Long, Operation> refundOperations = confirmedOperations
                .filter((key, value) -> value.getOperationType() == OperType.REFUND)
                .selectKey((key, value) -> value.getAccountId())
                .filter((key, value) -> {
                    try {
                        Account account = accountRepo.findById(value.getAccountId()).orElseThrow(() -> new NoSuchAccountException(value.getAccountId()));
                        BigDecimal newBalance = account.getBalance().add(value.getAmount());
                        setNewBalanceToAccount(account, newBalance);
                        return true;
                    } catch (Exception e) {
                        kafkaTemplate.send(RETRY_TOPIC, value);
                        return false;
                    }
                });
        refundOperations.to(DLG_SUCCEED, Produced.with(Serdes.Long(), new JsonSerde<>(Operation.class)));

        KStream<Long, BigDecimal> withdrawalOperations = confirmedOperations
                .filter((key, value) -> value.getOperationType() == OperType.WITHDRAWAL)
                .selectKey((key, value) -> value.getId())
                .mapValues((key, value) -> {
                    try {
                        Account account = accountRepo.findById(value.getAccountId()).orElseThrow(() -> new NoSuchAccountException(value.getAccountId()));
                        return account.getBalance().subtract(value.getAmount());
                    } catch (Exception e) {
                        kafkaTemplate.send(RETRY_TOPIC, value);
                        return null;
                    }
                })
                .filter((key, value) -> value != null);

        KStream<Long, Operation> withdrawalOperationsSuccess = withdrawalOperations
                .filter((key, value) -> value.compareTo(BigDecimal.ZERO) > 0)
                .mapValues((key, value) -> operationRepo.findById(key).orElseThrow(() -> new NoSuchOperationException(key)))
                .selectKey((key, value) -> value.getAccountId())
                .filter((key, value) -> {
                    try {
                        Account account = accountRepo.findById(value.getAccountId()).orElseThrow(() -> new NoSuchAccountException(value.getAccountId()));
                        BigDecimal newBalance = account.getBalance().subtract(value.getAmount());
                        setNewBalanceToAccount(account, newBalance);
                        return true;
                    } catch (Exception e) {
                        kafkaTemplate.send(RETRY_TOPIC, value);
                        return false;
                    }
                });
        withdrawalOperationsSuccess.to(DLG_SUCCEED, Produced.with(Serdes.Long(), new JsonSerde<>(Operation.class)));

        KStream<Long, Operation> withdrawalOperationsFail = withdrawalOperations
                .filter((key, value) -> value.compareTo(BigDecimal.ZERO) < 0)
                .mapValues((key, value) -> operationRepo.findById(key).orElseThrow(() -> new NoSuchOperationException(key)))
                .selectKey((key, value) -> value.getAccountId());
        withdrawalOperationsFail.to(DLG_FAILED, Produced.with(Serdes.Long(), new JsonSerde<>(Operation.class)));
    }

    private void setNewBalanceToAccount(Account account, BigDecimal newBalance) {
        account.setBalance(newBalance);
        accountRepo.save(account);
    }
}
