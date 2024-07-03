package com.example.kafka.kafka.processorApi;

import com.example.kafka.dto.OperType;
import com.example.kafka.entity.Account;
import com.example.kafka.entity.Operation;
import com.example.kafka.exception.NoSuchAccountException;
import com.example.kafka.repository.jpa.AccountRepo;
import com.example.kafka.repository.jpa.OperationRepo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;

import static com.example.kafka.config.KafkaTopicConfig.RETRY;
import static com.example.kafka.kafka.processorApi.JoinProcessor.setIsConfirmedForOperation;

@Slf4j
@AllArgsConstructor
public class RefundProcessor implements Processor<Long, Operation, Long, Operation> {

    private ProcessorContext<Long, Operation> context;
    private final OperationRepo operationRepo;
    private final AccountRepo accountRepo;
    private final KafkaTemplate<Long, Operation> kafkaTemplate;

    @Override
    public void init(ProcessorContext<Long, Operation> context) {
        this.context = context;
    }

    public RefundProcessor(OperationRepo operationRepo, AccountRepo accountRepo, KafkaTemplate<Long, Operation> kafkaTemplate) {
        this.operationRepo = operationRepo;
        this.accountRepo = accountRepo;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void process(Record<Long, Operation> record) {
        try {
            if (record.value().getOperationType() == OperType.REFUND) {
                setIsConfirmedForOperation(record, operationRepo);
                Account account = accountRepo.findById(record.value().getAccountId())
                        .orElseThrow(() -> new NoSuchAccountException(record.value().getAccountId()));
                BigDecimal newBalance = account.getBalance().add(record.value().getAmount());
                account.setBalance(newBalance);
                accountRepo.save(account);
                context.forward(record);
                context.commit();
            }
        } catch (Exception e) {
            kafkaTemplate.send(RETRY, record.value());
        }
    }
}

