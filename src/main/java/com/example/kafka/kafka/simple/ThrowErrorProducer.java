package com.example.kafka.kafka.simple;

import com.example.kafka.entity.Operation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class ThrowErrorProducer {

    private KafkaTemplate<Long, Operation> kafkaTemplate;

    public void send(Long accId, Operation operation, String topic) {
        log.info("Sending message to the error topic ...");
        kafkaTemplate.send(topic, accId, operation);
    }
}
