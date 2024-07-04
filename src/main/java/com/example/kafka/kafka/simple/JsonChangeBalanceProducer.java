package com.example.kafka.kafka.simple;

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

    public void send(List<Operation> operations, String topic) {
        log.info("Sending message to change balance to the topic ...");
        operations.forEach(operation -> kafkaTemplate.send(topic, operation.getAccountId(), operation));
    }

}
