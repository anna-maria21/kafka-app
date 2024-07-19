package com.example.kafka;

import com.example.kafka.entity.Operation;
import com.example.kafka.exception.NoSuchOperationException;
import com.example.kafka.kafka.simple.ConfirmProducer;
import com.example.kafka.kafka.simple.JsonChangeBalanceProducer;
import com.example.kafka.kafka.simple.ThrowErrorProducer;
import com.example.kafka.repository.jpa.OperationRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProducersTests {
    public static final String TOPIC = "test-topic";

    private KafkaTemplate<Long, Operation> templateMock;
    private Operation operation;

    @Mock
    private OperationRepo operationRepo;


    @BeforeEach
    public void setup() {
        templateMock = mock(KafkaTemplate.class);
        operation = PaymentsServiceTests.getTestOperation(1L, 1L, false);
    }


    @Test
    public void jsonChangeBalanceProducerTest() {
        List<Operation> operations = new ArrayList<>();
        operations.add(operation);
        JsonChangeBalanceProducer producer = new JsonChangeBalanceProducer(templateMock);
        producer.send(operations, TOPIC);

        verify(templateMock).send(TOPIC, operation.getAccountId(), operation);
    }

    @Test
    public void confirmProducerTest() {
        LinkedList<Integer> confirmedIds = new LinkedList<>();
        confirmedIds.add(1);
        when(operationRepo.findById(1L)).thenReturn(Optional.of(operation));
        ConfirmProducer producer = new ConfirmProducer(templateMock, operationRepo);
        producer.send(confirmedIds, TOPIC);

        verify(templateMock).send(TOPIC, operation.getId(), operation);
    }

    @Test
    public void confirmProducerWithInvalidIdsTest() {
        LinkedList<Integer> confirmedIds = new LinkedList<>();
        confirmedIds.add(1);
        when(operationRepo.findById(1L)).thenReturn(Optional.empty());
        ConfirmProducer producer = new ConfirmProducer(templateMock, operationRepo);

        assertThrows(NoSuchOperationException.class, () -> producer.send(confirmedIds, TOPIC));
    }

    @Test
    public void throwErrorProducerTest() {
        ThrowErrorProducer producer = new ThrowErrorProducer(templateMock);
        producer.send(operation.getAccountId(), operation, TOPIC);

        verify(templateMock).send(TOPIC, operation.getId(), operation);
    }
}
