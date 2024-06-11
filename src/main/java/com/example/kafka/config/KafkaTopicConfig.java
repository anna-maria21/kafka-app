package com.example.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic balanceChangesTopic() {
        return TopicBuilder.name("change-balance")
//                .partitions(2)
                .config(TopicConfig.RETENTION_MS_CONFIG, "600000")
                .build();
    }
    @Bean
    public NewTopic dlgTopic() {
        return TopicBuilder.name("dialog")
                .config(TopicConfig.RETENTION_MS_CONFIG, "6000")
                .partitions(3)
                .build();
    }
}
