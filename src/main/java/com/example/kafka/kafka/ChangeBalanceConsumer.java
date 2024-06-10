package com.example.kafka.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ChangeBalanceConsumer {

    @KafkaListener(topics = "change-balance", groupId = "myConsGroup")
    public void consume(String message) {
        log.info("Message received: {}", message);
    }
}
