package com.example.kafka.config;

import com.example.kafka.entity.Operation;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaStreamsConfig {

    @Bean
    public JsonSerde<Operation> operationJsonSerde() {
        JsonSerde<Operation> serde = new JsonSerde<>(Operation.class);
        serde.configure(Map.of(JsonDeserializer.VALUE_DEFAULT_TYPE, Operation.class.getName()), false);
        return serde;
    }

//    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
//    KafkaStreamsConfiguration kStreamsConfig() {
//        Map<String, Object> props = new HashMap<>();
//        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "spring-boot-streams");
//        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
//        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.LongSerde.class.getName());
//        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, JsonSerde.class.getName());
//
////        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.Long().getClass().getName());
////        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, JsonSerde.class.getName());
//        props.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, LogAndContinueExceptionHandler.class.getName());
//
//        return new KafkaStreamsConfiguration(props);
//    }

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    KafkaStreamsConfiguration kStreamsConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "spring-boot-streams");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, 1);
        props.put(StreamsConfig.BUFFERED_RECORDS_PER_PARTITION_CONFIG, 1);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.LongSerde.class.getName());
//        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, JsonSerde.class.getName());

        // Custom properties for JSON Serde
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.example.kafka.entity");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.example.kafka.entity.Operation");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        // Configuring value deserializer to use ErrorHandlingDeserializer
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, ErrorHandlingDeserializer.class.getName());
        props.put("spring.deserializer.value.delegate.class", JsonDeserializer.class.getName());

        // Exception handling configuration
        props.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, "com.example.kafka.exception.MyStreamsDeserializationExceptionHandler");

        return new KafkaStreamsConfiguration(props);
    }

    @Bean
    public StoreBuilder<KeyValueStore<Long, Operation>> topicAStoreBuilder() {
        return Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore("change-balance-store"),
                Serdes.Long(),
                new JsonSerde<>(Operation.class));
    }

    @Bean
    public StoreBuilder<KeyValueStore<Long, Operation>> topicBStoreBuilder() {
        return Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore("confirmation-store"),
                Serdes.Long(),
                new JsonSerde<>(Operation.class));
    }
}


