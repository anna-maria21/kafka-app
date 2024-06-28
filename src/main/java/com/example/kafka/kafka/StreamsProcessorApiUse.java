package com.example.kafka.kafka;

import com.example.kafka.entity.Operation;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.apache.kafka.common.serialization.Serdes;
import com.example.kafka.repository.jpa.AccountRepo;
import com.example.kafka.repository.jpa.OperationRepo;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.apache.kafka.streams.processor.api.Record;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final JsonSerde<Operation> operationJsonSerde;

//    @Autowired
    @PostConstruct
    public void process() {

        Topology topology = new Topology();

        topology.addSource("ChangeBalanceSource", "change-balance")
                .addSource("PaymentConfirmationSource", "payment-confirmation");

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

//@Component
//@Slf4j
//@AllArgsConstructor
//public class StreamsProcessorApiUse implements ProcessorSupplier<Long, Operation, Long, Operation> {
//
//    private final OperationProcessor operationProcessor;
//    private final ConfirmProcessor confirmProcessor;
//
//    @Override
//    public Processor<Long, Operation, Long, Operation> get() {
//        return new Processor<Long, Operation, Long, Operation>() {
//            private ProcessorContext context;
//
//            @Override
//            public void init(ProcessorContext context) {
//                this.context = context;
//            }
//
//            @Override
//            public void process(Record<Long, Operation> record) {
////                Long key = record.key();
////                Operation operation = record.value();
//
//                // Process change-balance topic
//                operationProcessor.process(record);
//
//                // Process payment-confirmation topic
//                context.forward(record);
//            }
//
//            @Override
//            public void close() {
//                // Cleanup logic (optional)
//            }
//        };
//    }
//}
