package com.example.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {


    public static final String CHANGE_BALANCE = "change-balance";
    public static final String CONFIRMATION   = "payment-confirmation";
    public static final String DLG_FAILED     = "dlg-failed";
    public static final String DLG_SUCCEED    = "dlg-succeed";
    public static final String RETRY          = "my-retry";
    public static final String DLQ            = "dlq";

    @Bean
    public NewTopic balanceChangesTopic() {
        return TopicBuilder.name(CHANGE_BALANCE)
                .partitions(5)
                .config(TopicConfig.RETENTION_MS_CONFIG, "600000")
                .build();
    }

    @Bean
    public NewTopic paymentConfirmationTopic() {
        return TopicBuilder.name(CONFIRMATION)
                .config(TopicConfig.RETENTION_MS_CONFIG, "600000")
                .partitions(5)
                .build();
    }

    @Bean
    public NewTopic dlgSucceedTopic() {
        return TopicBuilder.name(DLG_SUCCEED)
                .config(TopicConfig.RETENTION_MS_CONFIG, "600000")
                .partitions(5)
                .build();
    }

    @Bean
    public NewTopic dlgFailedTopic() {
        return TopicBuilder.name(DLG_FAILED)
                .config(TopicConfig.RETENTION_MS_CONFIG, "60000")
                .partitions(5)
                .build();
    }

    @Bean
    public NewTopic retry() {
        return TopicBuilder.name(RETRY)
                .config(TopicConfig.RETENTION_MS_CONFIG, "60000")
                .partitions(3)
                .build();
    }

    @Bean
    public NewTopic dlq() {
        return TopicBuilder.name(DLQ)
                .config(TopicConfig.RETENTION_MS_CONFIG, "60000")
                .build();
    }

}
