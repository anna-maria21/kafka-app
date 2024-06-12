package com.example.kafka.controller;

import com.example.kafka.dto.AccountDto;
import com.example.kafka.dto.OperationDto;
import com.example.kafka.entity.Account;
import com.example.kafka.entity.Person;
import com.example.kafka.kafka.JsonChangeBalanceProducer;
import com.example.kafka.service.JsonBalanceService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedList;

@RestController
@RequestMapping("api/kafka")
@AllArgsConstructor
public class JsonBalanceController {

    private final JsonChangeBalanceProducer changeBalanceProducer;
    private final JsonBalanceService service;


    @PostMapping("/new/acc")
    public Account newAccount(@RequestBody AccountDto accountDto) {
        return service.saveNewAccount(accountDto);
    }

    @PostMapping("/new/person")
    public Person newPerson(@RequestBody Person person) {
        return service.saveNewPerson(person);
    }

    @PostMapping("/change-balance")
    public ResponseEntity<String> sendMessage(@RequestBody LinkedList<OperationDto> input) {
        changeBalanceProducer.send(input);
        return ResponseEntity.ok("JSON input sent to the topic");
    }

}
