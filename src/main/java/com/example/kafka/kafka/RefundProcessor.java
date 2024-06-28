package com.example.kafka.kafka;

import com.example.kafka.entity.Account;
import com.example.kafka.entity.Operation;
import com.example.kafka.exception.NoSuchAccountException;
import com.example.kafka.repository.jpa.AccountRepo;
import lombok.AllArgsConstructor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.Record;

import java.math.BigDecimal;

@AllArgsConstructor
public class RefundProcessor implements Processor<Long, Operation, Long, Operation> {

    private ProcessorContext<Long, Operation> context;
    private final AccountRepo accountRepo;

    @Override
    public void init(ProcessorContext<Long, Operation> context) {
        this.context = context;
    }

    public RefundProcessor(AccountRepo accountRepo) {
        this.accountRepo = accountRepo;
    }

    @Override
    public void process(Record<Long, Operation> record) {
        Account account = accountRepo.findById(record.value().getAccountId())
                .orElseThrow(() -> new NoSuchAccountException(record.value().getAccountId()));
        BigDecimal newBalance = account.getBalance().add(record.value().getAmount());
        account.setBalance(newBalance);
        accountRepo.save(account);
        context.forward(record);
        context.commit();
    }

    @Override
    public void close() {}
}

