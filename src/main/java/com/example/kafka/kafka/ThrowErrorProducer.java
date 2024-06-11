package com.example.kafka.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ThrowErrorProducer {

    private KafkaTemplate<String, String> kafkaTemplate;

    public ThrowErrorProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(String message) {
        log.info("Sending message to get error to the topic ...");
        kafkaTemplate.send("dialog", message);
    }
}
