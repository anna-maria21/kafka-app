package com.example.kafka.kafka;

import com.example.kafka.entity.Operation;
import com.example.kafka.repository.jpa.OperationRepo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.Record;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OperationProcessor implements Processor<Long, Operation, Long, Operation> {

    private ProcessorContext<Long, Operation> context;
    private final OperationRepo operationRepo;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String HASH_KEY = "Operation";

    public OperationProcessor(OperationRepo operationRepo, RedisTemplate<String, Object> redisTemplate) {
        this.operationRepo = operationRepo;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void init(ProcessorContext<Long, Operation> context) {
        this.context = context;
    }

    @Override
    public void process(Record<Long, Operation> record) {
        log.info("Consumed from topic \"change-balance\": account - {}, operation - {}", record.key(), record.value());
        operationRepo.save(record.value());
        redisTemplate.opsForHash().put(HASH_KEY, record.key().toString(), record.value());
        context.forward(record);
        context.commit();
    }
    @Override
    public void close() {}


}

