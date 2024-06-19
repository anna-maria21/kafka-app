package com.example.kafka.kafka;

import com.example.kafka.dto.OperType;
import com.example.kafka.dto.OperationDto;
import com.example.kafka.entity.Account;
import com.example.kafka.entity.Operation;
import com.example.kafka.exception.NoSuchAccountException;
import com.example.kafka.mapper.Mapper;
import com.example.kafka.repository.AccountRepo;
import com.example.kafka.repository.OperationRepo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


@Service
@Slf4j
@AllArgsConstructor
public class JsonChangeBalanceConsumer {

    private final AccountRepo accountRepo;
    private final ThrowErrorProducer throwErrorProducer;
    private final OperationRepo operationRepo;
    private final Mapper mapper;

//    @KafkaListener(topics = "change-balance", groupId = "myConsGroup")
    public void consume(ConsumerRecord<Long, OperationDto> input) {

        log.info("Topic: \"change-balance\". Consumed: {}", input);

        Account account = accountRepo.findById(input.key())
                .orElseThrow(() -> new NoSuchAccountException(input.key()));
        log.info("Account {} balance: {}", input.key(), account.getBalance());

        OperationDto operationDto = input.value();
        Operation operation = mapper.toOperation(operationDto);
        operation = operationRepo.save(operation);
        log.info("Operation {}: {}: {}", operation.getId(), operation.getOperType(), operation.getAmount());

        if (operation.getOperType() == OperType.REFUND) {
            account.setBalance(account.getBalance().add(operation.getAmount()));
        } else if (operation.getOperType() == OperType.WITHDRAWAL) {
            BigDecimal newBalance = account.getBalance().subtract(operation.getAmount());
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                throwErrorProducer.send(input.key(), "Operation: " + operation.getId() +
                        " There are not enough funds in the account.");
            } else {
                account.setBalance(newBalance);
            }
        }

        accountRepo.save(account);
        log.info("Operation proceed. Account balance: {}", account.getBalance());
    }
}
