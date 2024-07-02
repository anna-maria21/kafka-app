package com.example.kafka.kafka;

import com.example.kafka.entity.Operation;
import com.example.kafka.repository.jpa.OperationRepo;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OperationProcessor implements Processor<Long, Operation, Long, Operation> {

    private static final String HASH_KEY = "Operation";
    private static final String CHANGE_BALANCE_TOPIC = "change-balance";

    private ProcessorContext<Long, Operation> context;
    private final OperationRepo operationRepo;
    private final RedisTemplate<String, Object> redisTemplate;
    private KeyValueStore<Long, Operation> store;

    public OperationProcessor(OperationRepo operationRepo, RedisTemplate<String, Object> redisTemplate) {
        this.operationRepo = operationRepo;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void init(ProcessorContext<Long, Operation> context) {
        this.context = context;
        store = context.getStateStore("change-balance-store");
    }

    @Override
    public void process(Record<Long, Operation> record) {
        record = new Record<>(record.key(), operationRepo.save(record.value()), record.timestamp());
        log.info("Consumed from topic {}: account - {}, operation - {}",
                CHANGE_BALANCE_TOPIC, record.key(), record.value());
//        redisTemplate.opsForHash().put(HASH_KEY, record.key().toString(), record.value());
//
//        log.info("saved operation to redis: {}", redisTemplate.opsForHash().get(HASH_KEY, record.key().toString()));

        store.put(record.value().getId(), record.value());
        log.info("{}", store.get(record.value().getId()));
        context.forward(record);
        context.commit();
    }

    @Override
    public void close() {}
}

