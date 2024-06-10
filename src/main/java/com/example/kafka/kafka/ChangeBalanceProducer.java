package com.example.kafka.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ChangeBalanceProducer {

    private KafkaTemplate<String, String> kafkaTemplate;

    public ChangeBalanceProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String message) {
        log.info("Sending message from {}: {}", ChangeBalanceProducer.class.getName(), message);
        kafkaTemplate.send("change-balance", message);
    }
}
