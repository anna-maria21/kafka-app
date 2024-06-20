package com.example.kafka.kafka;

import com.example.kafka.entity.Account;
import com.example.kafka.entity.Operation;
import com.example.kafka.exception.NoSuchAccountException;
import com.example.kafka.repository.AccountRepo;
import com.example.kafka.repository.OperationRepo;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class SuccessPaymentsConsumer {

    private final AccountRepo accountRepo;


    @KafkaListener(topics = "dlg-succeed", groupId = "myConsGroup")
    public void consume(ConsumerRecord<Long, Operation> record) {
        Operation operation = record.value();
        log.info("Acc id {}", record.key());
        // перевірити record.key() - приходить ід операції, а не ід акаунту
        Account account = accountRepo.findById(record.key()).orElseThrow(() -> new NoSuchAccountException(operation.getAccountId()));

        log.info("Topic: \"dlg-succeed\". Consumed new SUCCESS MESSAGE");
        log.info("Operation {}: {} {}. Account balance: {}", operation.getId(), operation.getOperationType(), operation.getAmount(), account.getBalance());
    }
}
