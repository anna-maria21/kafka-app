package com.example.kafka.kafka.simple;

import com.example.kafka.entity.Operation;
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

    @KafkaListener(topics = "dlg-failed", groupId = "myConsGroup")
    public void consume(ConsumerRecord<Long, Operation> record) {

        log.info("Topic: \"dlg-failed\". Consumed new ERROR MESSAGE");
        log.error("Operation: {}: {}. There are not enough funds in the account.", record.value().getId(), record.value().getOperationType());
    }
}
