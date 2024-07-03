package com.example.kafka.kafka.simple;

import com.example.kafka.entity.Operation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import static com.example.kafka.config.KafkaTopicConfig.DLG_SUCCEED;

@Service
@AllArgsConstructor
@Slf4j
public class SuccessPaymentsConsumer {

    @KafkaListener(topics = DLG_SUCCEED, groupId = "myConsGroup")
    public void consume(ConsumerRecord<Long, Operation> record) {
        Operation operation = record.value();

        log.info("Topic: \"dlg-succeed\". Consumed new SUCCESS MESSAGE");
        log.info("Operation {}: {} {}.", operation.getId(), operation.getOperationType(), operation.getAmount());
    }
}
