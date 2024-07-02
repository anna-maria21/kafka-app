package com.example.kafka.kafka;

import com.example.kafka.entity.Operation;
import com.example.kafka.exception.NoSuchOperationException;
import com.example.kafka.repository.jpa.OperationRepo;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.data.redis.core.RedisTemplate;

@Slf4j
public class JoinProcessor implements Processor<Long, Operation, Long, Operation> {
    private ProcessorContext<Long, Operation> context;

    private KeyValueStore<Long, Operation> changeBalanceStore;
    private KeyValueStore<Long, Operation> confirmationStore;

    private final OperationRepo operationRepo;

    public JoinProcessor(OperationRepo operationRepo) {
        this.operationRepo = operationRepo;
    }

    @Override
    public void init(ProcessorContext<Long, Operation> context) {
        this.context = context;
        changeBalanceStore = context.getStateStore("change-balance-store");
        confirmationStore = context.getStateStore("confirmation-store");
    }

    @Override
    public void process(Record<Long, Operation> confirmedRecord) {
        log.info("joining...");
        Operation operation = changeBalanceStore.get(confirmedRecord.key());
        log.info("from state store {}", operation);
        if (operation != null) {
            Operation o = operationRepo.findById(confirmedRecord.key())
                    .orElseThrow(() -> new NoSuchOperationException(confirmedRecord.key()));
            o.setIsConfirmed(true);
            operationRepo.save(o);
            context.forward(new Record<>(confirmedRecord.key(), o, confirmedRecord.timestamp()));
        }
        context.commit();
    }

    @Override
    public void close() {
        Processor.super.close();
    }
}
