package com.example.kafka.kafka.processorApi;

import com.example.kafka.entity.Operation;
import com.example.kafka.exception.NoSuchOperationException;
import com.example.kafka.repository.jpa.OperationRepo;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.springframework.data.redis.core.RedisTemplate;

import static com.example.kafka.config.RedisConfig.HASH_KEY;

@Slf4j
public class JoinProcessor implements Processor<Long, Operation, Long, Operation> {

    private ProcessorContext<Long, Operation> context;
    private final RedisTemplate<String, Object> redisTemplate;

    public JoinProcessor(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void init(ProcessorContext<Long, Operation> context) {
        this.context = context;
    }

    @Override
    public void process(Record<Long, Operation> confirmedRecord) {
        Operation operation = (Operation) redisTemplate.opsForHash().get(HASH_KEY, confirmedRecord.key().toString());
        if (operation != null) {
            redisTemplate.opsForHash().delete(HASH_KEY, confirmedRecord.key().toString());
            context.forward(new Record<>(operation.getId(), operation, confirmedRecord.timestamp()));
        }
        context.commit();
    }

    static void setIsConfirmedForOperation(Record<Long, Operation> record, OperationRepo operationRepo) {
        Operation o = operationRepo.findById(record.value().getId())
                .orElseThrow(() -> new NoSuchOperationException(record.value().getId()));
        o.setIsConfirmed(true);
        operationRepo.save(o);
    }
}
