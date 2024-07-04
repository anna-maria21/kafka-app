package com.example.kafka.kafka.simple;

import com.example.kafka.dto.OperType;
import com.example.kafka.entity.Account;
import com.example.kafka.entity.Operation;
import com.example.kafka.exception.NoSuchAccountException;
import com.example.kafka.repository.jpa.AccountRepo;
import com.example.kafka.repository.jpa.OperationRepo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static com.example.kafka.config.KafkaTopicConfig.CHANGE_BALANCE;
import static com.example.kafka.config.KafkaTopicConfig.DLG_FAILED;


@Service
@Slf4j
@AllArgsConstructor
public class JsonChangeBalanceConsumer {

    private final AccountRepo accountRepo;
    private final ThrowErrorProducer throwErrorProducer;
    private final OperationRepo operationRepo;

//    @KafkaListener(topics = CHANGE_BALANCE_TOPIC, groupId = "myConsGroup")
    public void consume(ConsumerRecord<Long, Operation> input) {

        log.info("Topic: {}. Consumed: {}", CHANGE_BALANCE, input);

        Account account = accountRepo.findById(input.key())
                .orElseThrow(() -> new NoSuchAccountException(input.key()));
        log.info("Account {} balance: {}", input.key(), account.getBalance());

        Operation operation = input.value();
        operation = operationRepo.save(operation);
        log.info("Operation {}: {}: {}", operation.getId(), operation.getOperationType(), operation.getAmount());

        if (operation.getOperationType() == OperType.REFUND) {
            account.setBalance(account.getBalance().add(operation.getAmount()));
        } else if (operation.getOperationType() == OperType.WITHDRAWAL) {
            BigDecimal newBalance = account.getBalance().subtract(operation.getAmount());
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                throwErrorProducer.send(input.key(), input.value(), DLG_FAILED);
            } else {
                account.setBalance(newBalance);
            }
        }

        accountRepo.save(account);
        log.info("Operation proceed. Account balance: {}", account.getBalance());
    }
}
