package com.example.kafka.controller;

import com.example.kafka.dto.AccountDto;
import com.example.kafka.entity.Account;
import com.example.kafka.entity.Operation;
import com.example.kafka.entity.Person;
import com.example.kafka.service.JsonBalanceService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;

@RestController
@RequestMapping("api/kafka")
@AllArgsConstructor
public class JsonBalanceController {

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
    public ResponseEntity<String> sendMessage(@RequestBody LinkedList<Operation> input) {
        service.sendPayments(input);
        return ResponseEntity.ok("JSON input sent to the topic");
    }

    @GetMapping("/confirm/{id}")
    public ResponseEntity<String> confirmPayment(@PathVariable LinkedList<Integer> id) {
        service.sendConfirmation(id);
        return ResponseEntity.ok("Confirmation sent to the topic");
    }

}
