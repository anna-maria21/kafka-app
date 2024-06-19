package com.example.kafka.kafka;

import com.example.kafka.dto.OperType;
import com.example.kafka.dto.OperationDto;
import com.example.kafka.entity.Account;
import com.example.kafka.entity.Operation;
import com.example.kafka.exception.NoSuchAccountException;
import com.example.kafka.mapper.Mapper;
import com.example.kafka.repository.AccountRepo;
import com.example.kafka.repository.OperationRepo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
@AllArgsConstructor
public class OperationTopology {

    private final Mapper mapper;
    private final AccountRepo accountRepo;
    private final OperationRepo operationRepo;

    @Autowired
    public void process(StreamsBuilder builder) {

        KStream<Long, Operation> operStream = builder
                .stream("change-balance", Consumed.with(Serdes.Long(), new JsonSerde<>(OperationDto.class)))
                .mapValues(value -> operationRepo.save(mapper.toOperation(value)))
                .peek((key, value) -> log.info("Consumed from topic \"change-balance\": key - {}, value - {}", key, value));

        KStream<Long, Operation> refundOperations = operStream
                .filter((key, value) -> value.getOperType() == OperType.REFUND)
                .peek((key, value) -> {
                    Account account = value.getAccount();
                    BigDecimal newBalance = account.getBalance().add(value.getAmount());
                    setNewBalanceToAccount(account, value, newBalance);
                });

        KStream<Long, BigDecimal> withdrawalOperations = operStream
                .filter((key, value) -> value.getOperType() == OperType.WITHDRAWAL)
                .selectKey((key, value) -> value.getId())
                .mapValues((key, value) -> {
                    Account account = value.getAccount();
                    return account.getBalance().subtract(value.getAmount());
                });

        KStream<Long, Operation> withdrawalOperationsSuccess = withdrawalOperations
                .filter((key, value) -> value.compareTo(BigDecimal.ZERO) > 0)
                .mapValues((key, value) -> operationRepo.findById(key).orElseThrow(() -> new NoSuchAccountException(1L)))
                .selectKey((key, value) -> value.getAccount().getId())
                .peek((key, value) -> {
                    Account account = value.getAccount();
                    BigDecimal newBalance = account.getBalance().subtract(value.getAmount());
                    setNewBalanceToAccount(account, value, newBalance);
                });

        KStream<Long, String> withdrawalOperationsFail = withdrawalOperations
                .filter((key, value) -> value.compareTo(BigDecimal.ZERO) < 0)
                .mapValues((key, value) -> operationRepo.findById(key).orElseThrow(() -> new NoSuchAccountException(1L)))
                .selectKey((key, value) -> value.getAccount().getId())
                .mapValues(value -> "Operation: " + value.getId() + ": " + value.getOperType() + ". There are not enough funds in the account.");

        withdrawalOperationsFail.to("dialog", Produced.with(Serdes.Long(), Serdes.String()));
    }

    private void setNewBalanceToAccount(Account account, Operation operation, BigDecimal newBalance) {
        account.setBalance(newBalance);
        accountRepo.save(account);
        log.info("Operation {}: {} {}. Account balance: {}", operation.getId(), operation.getOperType(), operation.getAmount(), account.getBalance());
    }
}
