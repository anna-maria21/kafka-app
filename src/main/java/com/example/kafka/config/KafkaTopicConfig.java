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
                .config(TopicConfig.RETENTION_MS_CONFIG, "360000000")
                .build();
    }

    @Bean
    public NewTopic paymentConfirmationTopic() {
        return TopicBuilder.name("payment-confirmation")
                .config(TopicConfig.RETENTION_MS_CONFIG, "600000")
                .partitions(5)
                .build();
    }

//    @Bean
//    public NewTopic tempTopic() {
//        return TopicBuilder.name("temp")
//                .config(TopicConfig.RETENTION_MS_CONFIG, "600000")
//                .partitions(5)
//                .build();
//    }

    @Bean
    public NewTopic dlgTopic() {
        return TopicBuilder.name("dialog")
                .config(TopicConfig.RETENTION_MS_CONFIG, "60000")
                .partitions(5)
                .build();
    }

}
