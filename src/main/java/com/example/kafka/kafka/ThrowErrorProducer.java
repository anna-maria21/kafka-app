package com.example.kafka.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ThrowErrorProducer {

    private KafkaTemplate<String, String> kafkaTemplate;

    public ThrowErrorProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(String message) {
        kafkaTemplate.send("dialog", message);
    }
}
