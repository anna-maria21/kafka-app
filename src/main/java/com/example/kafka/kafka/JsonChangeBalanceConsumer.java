package com.example.kafka.kafka;

import com.example.kafka.dto.Account;
import com.example.kafka.dto.OperType;
import com.example.kafka.dto.Operation;
import com.example.kafka.exception.NoSuchAccountException;
import com.example.kafka.repository.AccountRepo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


@Service
@Slf4j
@AllArgsConstructor
public class JsonChangeBalanceConsumer {

    private final AccountRepo accountRepo;
    private final ThrowErrorProducer throwErrorProducer;

    @KafkaListener(topics = "change-balance", groupId = "myConsGroup")
    public void consume(ConsumerRecord<Long, Operation> input) {

        log.info("Topic: \"change-balance\". Consumed: {}", input);

        Account account = accountRepo.findById(input.key())
                .orElseThrow(() -> new NoSuchAccountException(input.key()));

        log.info("Account balance: {}", account.getBalance());

        Operation operation = input.value();

        log.info("Operation: {}: {}", operation.getOperType(), operation.getAmount());
        if (operation.getOperType() == OperType.REFUND) {
            account.setBalance(account.getBalance().add(operation.getAmount()));
        } else if (operation.getOperType() == OperType.WITHDRAWAL) {
            BigDecimal newBalance = account.getBalance().subtract(operation.getAmount());
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                throwErrorProducer.send(input.key(), /*"Operation: " + operation.getOperType() + " Sum: " + operation.getAmount() + */" There are not enough funds in the account. Current balance: " + account.getBalance());
            } else {
                account.setBalance(newBalance);
            }
        }
        accountRepo.save(account);
        log.info("Operation proceed. Account balance: {}", account.getBalance());

    }
}
