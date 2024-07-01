package com.example.kafka.kafka;

import com.example.kafka.entity.Operation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class JsonChangeBalanceProducer {

    private KafkaTemplate<Long, Operation> kafkaTemplate;
    public static final String CHANGE_BALANCE_TOPIC = "change-balance";

    public void send(List<Operation> operations) {
        log.info("Sending message to change balance to the topic ...");
        operations.forEach(operation -> kafkaTemplate.send(CHANGE_BALANCE_TOPIC, operation.getAccountId(), operation));
    }

}
