package com.example.kafka.kafka;

import com.example.kafka.entity.Operation;
import com.example.kafka.repository.jpa.AccountRepo;
import com.example.kafka.repository.jpa.OperationRepo;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static com.example.kafka.config.KafkaTopicConfig.*;

@Component
@AllArgsConstructor
@Slf4j
public class StreamsProcessorApiUse {

    private final AccountRepo accountRepo;
    private final OperationRepo operationRepo;
    private final RedisTemplate<String, Object> redisTemplate;
    private final KafkaTemplate<Long, Operation> kafkaTemplate;
    private final KafkaStreamsConfiguration kafkaStreamsConfiguration;


    @PostConstruct
    public void process() {

        Topology topology = new Topology();

        topology.addSource("ChangeBalanceSource", CHANGE_BALANCE)
                .addSource("PaymentConfirmationSource", CONFIRMATION);

        topology.addProcessor("OperationProcessor",
                () -> new OperationProcessor(operationRepo, redisTemplate, kafkaTemplate),
                "ChangeBalanceSource");
        topology.addProcessor("ConfirmProcessor",
                ConfirmProcessor::new,
                "PaymentConfirmationSource");
        topology.addProcessor("JoinProcessor",
                () -> new JoinProcessor(operationRepo, redisTemplate),
                "ConfirmProcessor");
        topology.addProcessor("RefundProcessor",
                () -> new RefundProcessor(accountRepo),
                "JoinProcessor");
        topology.addProcessor("WithdrawalProcessor",
                () -> new WithdrawalProcessor(accountRepo, kafkaTemplate),
                "JoinProcessor");

        topology.addSink("SucceedSink", DLG_SUCCEED, "RefundProcessor", "WithdrawalProcessor");
        topology.addSink("FailedSink", DLG_FAILED, "WithdrawalProcessor");

        System.out.println(topology.describe());

        KafkaStreams streams = new KafkaStreams(topology, kafkaStreamsConfiguration.asProperties());
        streams.start();
    }
}
