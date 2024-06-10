package com.example.kafka.controller;

import com.example.kafka.dto.KafkaInput;
import com.example.kafka.kafka.JsonChangeBalanceProducer;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("api/kafka")
public class JsonBalanceController {

    private JsonChangeBalanceProducer changeBalanceProducer;

    public JsonBalanceController(JsonChangeBalanceProducer changeBalanceProducer) {
        this.changeBalanceProducer = changeBalanceProducer;
    }

    @PostMapping("/change-balance")
    public ResponseEntity<String> sendMessage(@RequestBody KafkaInput input) {
        changeBalanceProducer.send(input);
        return ResponseEntity.ok("JSON input sent to the topic");
    }

}
