package com.example.kafka.kafka;

import com.example.kafka.repository.jpa.AccountRepo;
import com.example.kafka.repository.jpa.OperationRepo;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
@AllArgsConstructor
@Slf4j
public class StreamsProcessorApiUse {
    private final AccountRepo accountRepo;
    private final OperationRepo operationRepo;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String CHANGE_BALANCE_TOPIC = "change-balance";
    private static final String CONFIRMATION_TOPIC = "payment-confirmation";

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
        topology.addProcessor("RefundProcessor",
                () -> new RefundProcessor(accountRepo),
                "ConfirmProcessor");
        topology.addProcessor("WithdrawalProcessor",
                () -> new WithdrawalProcessor(accountRepo),
                "ConfirmProcessor");

        topology.addSink("SucceedSink", "dlg-succeed", "RefundProcessor", "WithdrawalProcessor");
        topology.addSink("FailedSink", "dlg-failed", "WithdrawalProcessor");
        System.out.println("/////////////////" + topology.describe());

        KafkaStreams streams = new KafkaStreams(topology, getStreamsConfig());
        streams.start();
    }

    private Properties getStreamsConfig() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "spring-boot-streams");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.Long().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, JsonSerde.class.getName());
        return props;
    }
}
