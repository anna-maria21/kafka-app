package com.example.kafka.kafka.processorApi;

import com.example.kafka.entity.Operation;
import com.example.kafka.repository.jpa.OperationRepo;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.TimeUnit;

import static com.example.kafka.config.KafkaTopicConfig.CHANGE_BALANCE;
import static com.example.kafka.config.KafkaTopicConfig.RETRY;
import static com.example.kafka.config.RedisConfig.HASH_KEY;

@Slf4j
public class OperationProcessor implements Processor<Long, Operation, Long, Operation> {

    private ProcessorContext<Long, Operation> context;
    private final OperationRepo operationRepo;
    private final RedisTemplate<String, Object> redisTemplate;
    private final KafkaTemplate<Long, Operation> kafkaTemplate;

    public OperationProcessor(OperationRepo operationRepo, RedisTemplate<String, Object> redisTemplate, KafkaTemplate<Long, Operation> kafkaTemplate) {
        this.operationRepo = operationRepo;
        this.redisTemplate = redisTemplate;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void init(ProcessorContext<Long, Operation> context) {
        this.context = context;
    }

    @Override
    public void process(Record<Long, Operation> record) {
        try {
            Operation o = operationRepo.save(record.value());
            record = new Record<>(record.key(), o, record.timestamp());
            log.info("Consumed from topic {}: account - {}, operation - {}",
                    CHANGE_BALANCE, record.key(), record.value());
            redisTemplate.opsForHash().put(HASH_KEY, record.value().getId().toString(), record.value());
            redisTemplate.expire(record.value().getId().toString(), 7, TimeUnit.DAYS);
//            context.forward(record);
            context.commit();
        } catch (Exception e) {
            kafkaTemplate.send(RETRY, record.value());
        }
    }
}

