package com.example.kafka.kafka;

import com.example.kafka.entity.Operation;
import com.example.kafka.repository.OperationRepo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class ConfirmProducer {

    private KafkaTemplate<Long, Operation> kafkaTemplate;
    private OperationRepo operationRepo;

    public void send(Long operationId) {
        log.info("Sending message to the confirmation topic ...");
        kafkaTemplate.send("payment-confirmation", operationId, operationRepo.findById(operationId).orElseThrow(() -> new RuntimeException("Operation not found")));
    }
}
