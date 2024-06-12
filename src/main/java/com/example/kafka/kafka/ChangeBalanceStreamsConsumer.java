package com.example.kafka.kafka;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
@AllArgsConstructor
@NoArgsConstructor
@EnableKafkaStreams
public class ChangeBalanceStreamsConsumer {

    private KafkaStreams kafkaStreams;

    @PostConstruct
    public void startKafkaStreams() {
        Properties props = new Properties();
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, "org.apache.kafka.common.serialization.Serdes$StringSerde");
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, "org.apache.kafka.common.serialization.Serdes$StringSerde");

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> sourceStream = builder.stream("change-balance");
        KStream<String, String> processedStream = sourceStream
                .filter((key, value) -> value.contains("important"));
        processedStream.to("processed-topic");

        Topology topology = builder.build();
        kafkaStreams = new KafkaStreams(topology, props);
        kafkaStreams.start();
    }

//    public

    @PreDestroy
    public void closeKafkaStreams() {
        if (kafkaStreams != null) {
            kafkaStreams.close();
        }
    }
}
