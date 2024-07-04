package com.example.kafka.service;

import com.example.kafka.entity.Account;
import com.example.kafka.entity.Operation;
import com.example.kafka.entity.Person;
import com.example.kafka.exception.AlreadyConfirmedOperationException;
import com.example.kafka.exception.NoSuchAccountException;
import com.example.kafka.exception.NoSuchOperationException;
import com.example.kafka.exception.NoSuchPersonException;
import com.example.kafka.kafka.simple.ConfirmProducer;
import com.example.kafka.kafka.simple.JsonChangeBalanceProducer;
import com.example.kafka.repository.jpa.AccountRepo;
import com.example.kafka.repository.jpa.OperationRepo;
import com.example.kafka.repository.jpa.PersonRepo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedList;

import static com.example.kafka.config.KafkaTopicConfig.CHANGE_BALANCE;
import static com.example.kafka.config.KafkaTopicConfig.CONFIRMATION;

@Service
@AllArgsConstructor
@Slf4j
public class PaymentsService {

    private final PersonRepo personRepo;
    private final AccountRepo accountRepo;
    private final OperationRepo operationRepo;
    private final JsonChangeBalanceProducer changeBalanceProducer;
    private final ConfirmProducer confirmProducer;


    public Account saveNewAccount(Account account) {
        personRepo.findById(account.getPersonId()).orElseThrow(() -> new NoSuchPersonException(account.getPersonId()));
        return accountRepo.save(account);
    }

    public Person saveNewPerson(Person person) {
        return personRepo.save(person);
    }

    public void sendPayments(LinkedList<Operation> operations, String topic) {
        operations.forEach(o -> accountRepo.findById(o.getAccountId()).orElseThrow(() -> new NoSuchAccountException(o.getAccountId())));
        changeBalanceProducer.send(operations, topic);
    }

    public void sendConfirmation(LinkedList<Integer> ids, String topic) {
        long amountAlreadyConfirmedOperations = ids.stream()
                .filter(id -> operationRepo.findById(Long.valueOf(id)).orElseThrow(() -> new NoSuchOperationException(Long.valueOf(id))).getIsConfirmed())
                .count();
        if (amountAlreadyConfirmedOperations > 0) {
            throw new AlreadyConfirmedOperationException();
        }
        confirmProducer.send(ids, topic);
    }
}
