package com.example.kafka.mapper;

import com.example.kafka.dto.AccountDto;
import com.example.kafka.dto.OperationDto;
import com.example.kafka.entity.Account;
import com.example.kafka.entity.Operation;
import com.example.kafka.entity.Person;
import com.example.kafka.exception.NoSuchAccountException;
import com.example.kafka.exception.NoSuchPersonException;
import com.example.kafka.repository.AccountRepo;
import com.example.kafka.repository.PersonRepo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class Mapper {

    private final AccountRepo accountRepo;
    private final PersonRepo personRepo;

    public Operation toOperation(OperationDto operationDto) {
        Account acc = accountRepo.findById(operationDto.accId())
                .orElseThrow(() -> new NoSuchAccountException(operationDto.accId()));

        return Operation.builder()
                .id(operationDto.id())
                .operType(operationDto.operType())
                .amount(operationDto.amount())
                .account(acc)
                .build();
    }

    public Account toAccount(AccountDto accountDto) {
        Person person = personRepo.findById(accountDto.personId())
                .orElseThrow(() -> new NoSuchPersonException(accountDto.personId()));

        return Account.builder()
                .id(accountDto.id())
                .balance(accountDto.balance())
                .person(person)
                .build();
    }
}
