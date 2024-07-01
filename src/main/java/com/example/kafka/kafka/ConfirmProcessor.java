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

@Component
@Slf4j
public class ConfirmProcessor implements Processor<Long, Operation, Long, Operation> {

    public static final String CONFIRMATION = "payment-confirmation";
    private static final String HASH_KEY = "Operation";

    private ProcessorContext<Long, Operation> context;
    private final RedisTemplate<String, Object> redisTemplate;
    private final OperationRepo operationRepo;

    public ConfirmProcessor(OperationRepo operationRepo, RedisTemplate<String, Object> redisTemplate) {
        this.operationRepo = operationRepo;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void init(ProcessorContext<Long, Operation> context) {
        this.context = context;
    }

    @Override
    public void process(Record<Long, Operation> record) {
        log.info("Consumed from topic {}: account - {}, operation - {}", CONFIRMATION, record.key(), record.value());
        Operation redisOperation = (Operation) redisTemplate.opsForHash().get(HASH_KEY, record.value().getId().toString());
        if (redisOperation != null) {
            redisTemplate.opsForHash().delete(HASH_KEY, record.value().getId().toString());
            redisOperation.setIsConfirmed(true);
            operationRepo.save(redisOperation);
            context.forward(record);
        }
        context.commit();
    }

    @Override
    public void close() {}
}

