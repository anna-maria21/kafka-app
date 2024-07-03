package com.example.kafka.kafka;

import com.example.kafka.dto.OperType;
import com.example.kafka.entity.Account;
import com.example.kafka.entity.Operation;
import com.example.kafka.exception.NoSuchAccountException;
import com.example.kafka.repository.jpa.AccountRepo;
import lombok.AllArgsConstructor;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;

import static com.example.kafka.config.KafkaTopicConfig.DLG_FAILED;
import static com.example.kafka.config.KafkaTopicConfig.DLG_SUCCEED;

@AllArgsConstructor
public class WithdrawalProcessor implements Processor<Long, Operation, Long, Operation> {

    private ProcessorContext<Long, Operation> context;
    private final AccountRepo accountRepo;
    private final KafkaTemplate<Long, Operation> kafkaTemplate;

    public WithdrawalProcessor(AccountRepo accountRepo, KafkaTemplate<Long, Operation> kafkaTemplate) {
        this.accountRepo = accountRepo;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void init(ProcessorContext<Long, Operation> context) {
        this.context = context;
    }

    @Override
    public void process(Record<Long, Operation> record) {
        if (record.value().getOperationType() == OperType.WITHDRAWAL) {
            Account account = accountRepo.findById(record.value().getAccountId())
                    .orElseThrow(() -> new NoSuchAccountException(record.value().getAccountId()));
            BigDecimal newBalance = account.getBalance().subtract(record.value().getAmount());
            if (newBalance.compareTo(BigDecimal.ZERO) >= 0) {
                account.setBalance(newBalance);
                accountRepo.save(account);
                kafkaTemplate.send(DLG_SUCCEED, account.getId(), record.value());
            } else {
                kafkaTemplate.send(DLG_FAILED, account.getId(), record.value());
            }
            context.commit();
        }
    }

    @Override
    public void close() {
    }
}

