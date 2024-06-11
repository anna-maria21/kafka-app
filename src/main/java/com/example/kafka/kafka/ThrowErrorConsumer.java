package com.example.kafka.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
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

//    public static boolean messageReceived;

    @KafkaListener(topics = "dialog", groupId = "myConsGroup")
    public synchronized void consume(ConsumerRecord<Long, String> record) {

        log.info("Topic: \"dialog\". Consumed new ERROR MESSAGE");
        log.error("Account {}. {}", record.key(), record.value());
//        messageReceived = true;
//        notify();
    }
}
