package com.example.kafka.kafka;

import com.example.kafka.entity.Operation;
import com.example.kafka.repository.jpa.OperationRepo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedList;

@Service
@AllArgsConstructor
@Slf4j
public class ConfirmProducer {

    private KafkaTemplate<Long, Operation> kafkaTemplate;
    private OperationRepo operationRepo;

    public void send(LinkedList<Integer> operationIds) {
        log.info("Sending message to the confirmation topic ...");
        operationIds.forEach(id -> kafkaTemplate.send("payment-confirmation", Long.valueOf(id), operationRepo.findById(Long.valueOf(id)).orElseThrow(() -> new RuntimeException("Operation not found"))));
        }
}
