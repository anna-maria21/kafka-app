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

@Component
@Slf4j
public class ConfirmProcessor implements Processor<Long, Operation, Long, Operation> {

    private static final String HASH_KEY = "Operation";
    public static final String CONFIRMATION = "payment-confirmation";

    private ProcessorContext<Long, Operation> context;
    private final OperationRepo operationRepo;
    private final RedisTemplate<String, Object> redisTemplate;
    private KeyValueStore<Long, Operation> store;

    public ConfirmProcessor(OperationRepo operationRepo, RedisTemplate<String, Object> redisTemplate) {
        this.operationRepo = operationRepo;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void init(ProcessorContext<Long, Operation> context) {
        this.context = context;
        store = context.getStateStore("confirmation-store");
    }

    @Override
    public void process(Record<Long, Operation> record) {

        log.info("Consumed from topic {} Confirmed operation id: {}", CONFIRMATION, record.key());
//        Operation redisOperation = (Operation) redisTemplate.opsForHash().get(HASH_KEY, record.key().toString());
//        log.info("operation from redis {}", record.value());
//
//        if (redisOperation != null) {
//            redisTemplate.opsForHash().delete(HASH_KEY, record.value().getId().toString());
//            redisOperation.setIsConfirmed(true);
//            Operation updatedOperation = operationRepo.save(redisOperation);
//            context.forward(new Record<>(record.key(), updatedOperation, record.timestamp()));
//        })
        store.put(record.key(), record.value());
        context.forward(record);
        context.commit();
    }

    @Override
    public void close() {}
}

