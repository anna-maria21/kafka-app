package com.example.kafka.kafka.processorApi;

import com.example.kafka.dto.OperType;
import com.example.kafka.entity.Operation;
import com.example.kafka.exception.NoSuchOperationException;
import com.example.kafka.repository.jpa.OperationRepo;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;

import static com.example.kafka.config.KafkaTopicConfig.RETRY;
import static com.example.kafka.config.RedisConfig.HASH_KEY;

@Slf4j
public class JoinProcessor implements Processor<Long, Operation, Long, Operation> {

    private ProcessorContext<Long, Operation> context;
    private final RedisTemplate<String, Object> redisTemplate;
    private final KafkaTemplate<Long, Operation> kafkaTemplate;

    public JoinProcessor(RedisTemplate<String, Object> redisTemplate, KafkaTemplate<Long, Operation> kafkaTemplate) {
        this.redisTemplate = redisTemplate;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void init(ProcessorContext<Long, Operation> context) {
        this.context = context;
    }

    @Override
    public void process(Record<Long, Operation> confirmedRecord) {
        try {
            Operation operation = (Operation) redisTemplate.opsForHash().get(HASH_KEY, confirmedRecord.key().toString());
            if (operation != null) {
                if (operation.getOperationType() != OperType.REFUND && operation.getOperationType() != OperType.WITHDRAWAL) {
                    throw new IllegalArgumentException();
                }
                redisTemplate.opsForHash().delete(HASH_KEY, confirmedRecord.key().toString());
                context.forward(new Record<>(operation.getId(), operation, confirmedRecord.timestamp()));
            }
            context.commit();
        } catch (Exception e) {
            kafkaTemplate.send(RETRY, confirmedRecord.value());
        }
    }

    static void setIsConfirmedForOperation(Record<Long, Operation> record, OperationRepo operationRepo) {
        Operation o = operationRepo.findById(record.value().getId())
                .orElseThrow(() -> new NoSuchOperationException(record.value().getId()));
        o.setIsConfirmed(true);
        operationRepo.save(o);
    }
}
