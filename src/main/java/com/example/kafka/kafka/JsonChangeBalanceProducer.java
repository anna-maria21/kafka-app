package com.example.kafka.kafka;

import com.example.kafka.dto.Account;
import com.example.kafka.dto.KafkaInput;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class JsonChangeBalanceProducer {

    private KafkaTemplate<String, Account> kafkaTemplate;

    public void send(KafkaInput kafkaInput) {
        log.info("Sending message to change balance to the topic ...");

        Message<KafkaInput> input = MessageBuilder
                .withPayload(kafkaInput)
                .setHeader(KafkaHeaders.TOPIC, "change-balance")
                .build();

        kafkaTemplate.send(input);
    }
}
