package com.example.kafka.kafka;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
@Getter
@Setter
public class ThrowErrorConsumer {

    @KafkaListener(topics = "dialog", groupId = "myConsGroup")
    public void consume(ConsumerRecord<Long, String> record) {

        log.info("Topic: \"dialog\". Consumed new ERROR MESSAGE");
        log.error("Account {}. {}", record.key(), record.value());
    }
}
