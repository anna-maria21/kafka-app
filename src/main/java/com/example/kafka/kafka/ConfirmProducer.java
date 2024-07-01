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
    public static final String CONFIRMATION = "payment-confirmation";

    public void send(LinkedList<Integer> operationIds) {
        log.info("Sending message to the {} topic ...", CONFIRMATION);
        operationIds.forEach(id -> kafkaTemplate.send(CONFIRMATION, Long.valueOf(id), operationRepo.findById(Long.valueOf(id)).orElseThrow(() -> new RuntimeException("Operation not found"))));
        }
}
