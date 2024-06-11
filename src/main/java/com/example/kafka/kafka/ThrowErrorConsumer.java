package com.example.kafka.kafka;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class ThrowErrorConsumer {

//    @Transactional
    @KafkaListener(topics = "dialog", groupId = "myConsGroup")
    public void consume(String message) {

        log.info("Topic: \"dialog\". Consumed new ERROR MESSAGE");
        log.error(message);
    }
}
