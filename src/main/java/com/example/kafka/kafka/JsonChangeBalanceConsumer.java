package com.example.kafka.kafka;

import com.example.kafka.dto.Account;
import com.example.kafka.dto.InputArrayItem;
import com.example.kafka.dto.KafkaInput;
import com.example.kafka.dto.OperType;
import com.example.kafka.exception.NoSuchAccountException;
import com.example.kafka.repository.AccountRepo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


@Service
@Slf4j
@AllArgsConstructor
public class JsonChangeBalanceConsumer {

    private final AccountRepo accountRepo;

    @KafkaListener(topics = "change-balance", groupId = "MyConsGroup")
    public void consume(KafkaInput input) {

        log.info("Consumed: {}", input);
        Account account = accountRepo.findById(input.getAccId())
                .orElseThrow(() -> new NoSuchAccountException(input.getAccId()));

        for (InputArrayItem operation : input.getOperations()) {
            if (operation.getOperType() == OperType.REFUND) {
                account.setBalance(account.getBalance().add(operation.getAmount()));
            }
            else if (operation.getOperType() == OperType.WITHDRAWAL) {
                account.setBalance(account.getBalance().subtract(operation.getAmount()));
            }
            log.info("Operation proceed. Account balance: {}", account.getBalance());
        }

    }
}
