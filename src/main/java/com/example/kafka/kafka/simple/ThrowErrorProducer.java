package com.example.kafka.kafka.simple;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class ThrowErrorProducer {

    private KafkaTemplate<Long, String> kafkaTemplate;

    public void send(Long accId, String message) {
        log.info("Sending message to the error topic ...");
        kafkaTemplate.send("dlg-failed", accId, message);
    }
}
