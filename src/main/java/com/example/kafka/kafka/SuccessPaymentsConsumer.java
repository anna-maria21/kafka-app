package com.example.kafka.kafka;

import com.example.kafka.entity.Account;
import com.example.kafka.entity.Operation;
import com.example.kafka.exception.NoSuchAccountException;
import com.example.kafka.repository.jpa.AccountRepo;
import lombok.AllArgsConstructor;
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
        Account account = accountRepo.findById(record.value().getAccountId()).orElseThrow(() -> new NoSuchAccountException(operation.getAccountId()));

        log.info("Topic: \"dlg-succeed\". Consumed new SUCCESS MESSAGE");
        log.info("Operation {}: {} {}.", operation.getId(), operation.getOperationType(), operation.getAmount());
    }
}
