package com.example.kafka.kafka;

import com.example.kafka.dto.OperType;
import com.example.kafka.dto.OperationDto;
import com.example.kafka.entity.Operation;
import com.example.kafka.mapper.Mapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Properties;

@Service
@AllArgsConstructor
public class ChangeBalanceStreamsConsumer {

//    private final KafkaStreams kafkaStreams;
    private final Mapper mapper;

//    @KafkaListener(topics = "change-balance", groupId = "myConsGroup")
//    public void startKafkaStreams() {
//
//        Properties props = new Properties();
//        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.Long().getClass());
//        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, "org.apache.kafka.common.serialization.Serdes$StringSerde");
//
//        StreamsBuilder builder = new StreamsBuilder();
//
//        KStream<Long, OperationDto> sourceStream = builder.stream("change-balance"/*,
//                Consumed.with(Serdes.Long(), Serdes.serdeFrom(null, new JsonDeserializer()))*/);
//
//
//        KStream<Long, Operation> processedStream = sourceStream
//                .mapValues((key, value) -> mapper.toOperation(value))
////                .filter((key, value) -> value.getOperType() == OperType.WITHDRAWAL || value.getOperType() == OperType.REFUND)
//                .peek((key, value) -> System.out.println(value));
//        processedStream.to("dialog");
//
//        Topology topology = builder.build();
//        KafkaStreams kafkaStreams = new KafkaStreams(topology, props);
//        kafkaStreams.start();
//    }

//    public

//    @PreDestroy
//    public void closeKafkaStreams() {
//        if (kafkaStreams != null) {
//            kafkaStreams.close();
//        }
//    }
}
