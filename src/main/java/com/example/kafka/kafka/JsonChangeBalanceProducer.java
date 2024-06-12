package com.example.kafka.kafka;

import com.example.kafka.dto.OperationDto;
import com.example.kafka.entity.Account;
import com.example.kafka.entity.Operation;
import com.example.kafka.exception.NoSuchAccountException;
import com.example.kafka.repository.AccountRepo;
import com.example.kafka.repository.OperationRepo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class JsonChangeBalanceProducer {

    private KafkaTemplate<Long, OperationDto> kafkaTemplate;


    public void send(LinkedList<OperationDto> operations) {
        log.info("Sending message to change balance to the topic ...");

//        Message<KafkaInput> input = MessageBuilder
//                .withPayload(kafkaInput)
//                .setHeader(KafkaHeaders.TOPIC, "change-balance")
//                .build();

        for (OperationDto operationDto : operations) {
            kafkaTemplate.send("change-balance", operationDto.accId(), operationDto);
        }
    }

}
