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

@Service
@AllArgsConstructor
@Getter
@Setter
@Slf4j
public class RetryConsumer {

    private final Map<Operation, Integer> notProcessed = new ConcurrentHashMap<>();
    private final KafkaTemplate<Long, Operation> kafkaTemplate;
    private static final String CHANGE_BALANCE_TOPIC = "change-balance";
    private static final String RETRY_TOPIC = "my-retry";
    private static final String DLQ_TOPIC = "dlq";

    @KafkaListener(topics = RETRY_TOPIC, groupId = "myConsGroup")
//    @RetryableTopic(attempts = "3", backoff = @Backoff(delay = 5000))
    public void consume(ConsumerRecord<Long, Operation> record) {
        log.info("Topic: \"my-retry\"");
        log.error("Trying to process Operation: {}", record.value());
        Operation operation = record.value();
        notProcessed.merge(operation, 1, Integer::sum);
        if (notProcessed.get(operation) < 3) {
            kafkaTemplate.send(CHANGE_BALANCE_TOPIC, operation.getAccountId(), operation);
        } else {
            kafkaTemplate.send(DLQ_TOPIC, operation);
        }
    }
}
