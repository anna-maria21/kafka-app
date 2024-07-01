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
                .partitions(5)
                .config(TopicConfig.RETENTION_MS_CONFIG, "600000")
                .build();
    }

    @Bean
    public NewTopic paymentConfirmationTopic() {
        return TopicBuilder.name("payment-confirmation")
                .config(TopicConfig.RETENTION_MS_CONFIG, "600000")
                .partitions(5)
                .build();
    }

    @Bean
    public NewTopic dlgSucceedTopic() {
        return TopicBuilder.name("dlg-succeed")
                .config(TopicConfig.RETENTION_MS_CONFIG, "600000")
                .partitions(5)
                .build();
    }

    @Bean
    public NewTopic dlgFailedTopic() {
        return TopicBuilder.name("dlg-failed")
                .config(TopicConfig.RETENTION_MS_CONFIG, "60000")
                .partitions(5)
                .build();
    }

    @Bean
    public NewTopic retry() {
        return TopicBuilder.name("my-retry")
                .config(TopicConfig.RETENTION_MS_CONFIG, "60000")
                .partitions(3)
                .build();
    }

    @Bean
    public NewTopic dlq() {
        return TopicBuilder.name("dlq")
                .config(TopicConfig.RETENTION_MS_CONFIG, "60000")
                .build();
    }

}
