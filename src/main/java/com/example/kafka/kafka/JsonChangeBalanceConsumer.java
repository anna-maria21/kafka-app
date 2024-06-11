package com.example.kafka.kafka;

import com.example.kafka.dto.Account;
import com.example.kafka.dto.InputArrayItem;
import com.example.kafka.dto.KafkaInput;
import com.example.kafka.dto.OperType;
import com.example.kafka.exception.NoSuchAccountException;
import com.example.kafka.repository.AccountRepo;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
//    @Transactional
    public void consume(KafkaInput input) {

        log.info("Topic: \"change-balance\". Consumed: {}", input);

        Account account = accountRepo.findById(input.getAccId())
                .orElseThrow(() -> new NoSuchAccountException(input.getAccId()));
        log.info("Account balance: {}", account.getBalance());

        for (InputArrayItem operation : input.getOperations()) {
            log.info("Operation: {}: {}", operation.getOperType(), operation.getAmount());
            if (operation.getOperType() == OperType.REFUND) {
                account.setBalance(account.getBalance().add(operation.getAmount()));
            } else if (operation.getOperType() == OperType.WITHDRAWAL) {
                BigDecimal newBalance = account.getBalance().subtract(operation.getAmount());
                if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                    throwErrorProducer.send("There are not enough funds in the account. Current balance: " + account.getBalance());
                } else {
                    account.setBalance(newBalance);
                }
            }
            accountRepo.save(account);
            log.info("Operation proceed. Account balance: {}", account.getBalance());
        }

    }
}
