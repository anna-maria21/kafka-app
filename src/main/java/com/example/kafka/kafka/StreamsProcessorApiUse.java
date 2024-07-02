package com.example.kafka.kafka;

import com.example.kafka.entity.Operation;
import com.example.kafka.repository.jpa.AccountRepo;
import com.example.kafka.repository.jpa.OperationRepo;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
@AllArgsConstructor
@Slf4j
public class StreamsProcessorApiUse {

    private static final String CHANGE_BALANCE_TOPIC = "change-balance";
    private static final String CONFIRMATION_TOPIC = "payment-confirmation";

    private final AccountRepo accountRepo;
    private final OperationRepo operationRepo;
    private final RedisTemplate<String, Object> redisTemplate;
    private final KafkaTemplate<Long, Operation> kafkaTemplate;
    private final StoreBuilder<KeyValueStore<Long, Operation>> topicAStoreBuilder;
    private final StoreBuilder<KeyValueStore<Long, Operation>> topicBStoreBuilder;
//    @Autowired
    @PostConstruct
    public void process() {

        Topology topology = new Topology();

        topology.addSource("ChangeBalanceSource", CHANGE_BALANCE_TOPIC)
                .addSource("PaymentConfirmationSource", CONFIRMATION_TOPIC);

        topology.addProcessor("OperationProcessor",
                () -> new OperationProcessor(operationRepo, redisTemplate),
                "ChangeBalanceSource");
        topology.addProcessor("ConfirmProcessor",
                () -> new ConfirmProcessor(operationRepo, redisTemplate),
                "PaymentConfirmationSource");
        topology.addProcessor("JoinProcessor",
                () -> new JoinProcessor(operationRepo),
                "ConfirmProcessor");
        topology.addProcessor("RefundProcessor",
                () -> new RefundProcessor(accountRepo),
                "JoinProcessor");
        topology.addProcessor("WithdrawalProcessor",
                () -> new WithdrawalProcessor(accountRepo, kafkaTemplate),
                "JoinProcessor");

        topology.addStateStore(topicAStoreBuilder, "OperationProcessor", "JoinProcessor");
        topology.addStateStore(topicBStoreBuilder, "ConfirmProcessor", "JoinProcessor");

        topology.addSink("SucceedSink", "dlg-succeed", "RefundProcessor", "WithdrawalProcessor");
        topology.addSink("FailedSink", "dlg-failed", "WithdrawalProcessor");

        System.out.println(topology.describe());

        KafkaStreams streams = new KafkaStreams(topology, getStreamsConfig());
        streams.start();
    }

    private Properties getStreamsConfig() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "spring-boot-streams");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, 1);
        props.put(StreamsConfig.BUFFERED_RECORDS_PER_PARTITION_CONFIG, 1);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.LongSerde.class.getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, JsonSerde.class.getName());

        // Custom properties for JSON Serde
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.example.kafka.entity");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.example.kafka.entity.Operation");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        props.put(StreamsConfig.STATE_DIR_CONFIG, "/tmp/kafka-streams");

        return props;
    }
}
