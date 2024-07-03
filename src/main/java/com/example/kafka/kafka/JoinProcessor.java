package com.example.kafka.kafka;

import com.example.kafka.entity.Operation;
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

    @Override
    public void close() {
    }
}
