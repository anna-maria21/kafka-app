package com.example.kafka.kafka.simple;

import com.example.kafka.entity.Operation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class DlqConsumer {

    @KafkaListener(topics = "dlq", groupId = "myConsGroup")
    public void consume(ConsumerRecord<Long, Operation> record) {
        log.info("Topic: \"dlq\"");
        log.error("Couldn't process Operation: {}", record.value());

    }
}
