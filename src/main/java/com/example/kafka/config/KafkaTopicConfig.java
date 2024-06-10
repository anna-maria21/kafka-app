package com.example.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic balanceChangesTopic() {
        return TopicBuilder.name("change-balance")
                //.partitions(3)
                .build();
    }
    @Bean
    public NewTopic dlgTopic() {
        return TopicBuilder.name("dialog")
                //.partitions(3)
                .build();
    }
}
