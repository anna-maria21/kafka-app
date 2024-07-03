package com.example.kafka.kafka;

import com.example.kafka.entity.Operation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.example.kafka.config.KafkaTopicConfig.*;

@Service
@AllArgsConstructor
@Getter
@Setter
@Slf4j
public class RetryConsumer {

    private final Map<Operation, Integer> notProcessed = new ConcurrentHashMap<>();
    private final KafkaTemplate<Long, Operation> kafkaTemplate;

    @KafkaListener(topics = RETRY, groupId = "myConsGroup")
//    @RetryableTopic(dltStrategy = DltStrategy.ALWAYS_RETRY_ON_ERROR)
    public void consume(ConsumerRecord<Long, Operation> record) {
        log.info("Topic: \"my-retry\"");
        log.error("Trying to process Operation: {}", record.value());
        Operation operation = record.value();
        notProcessed.merge(operation, 1, Integer::sum);
        if (notProcessed.get(operation) < 3) {
            kafkaTemplate.send(CHANGE_BALANCE, operation.getAccountId(), operation);
        } else {
            kafkaTemplate.send(DLQ, operation);
        }
    }
}
