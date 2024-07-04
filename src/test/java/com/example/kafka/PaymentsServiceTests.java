package com.example.kafka;

import com.example.kafka.dto.OperType;
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
import com.example.kafka.service.PaymentsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentsServiceTests {

    public static final Long PERSON_ID = 1L;
    public static final Long ACCOUNT_ID = 1L;
    private static final Long OPERATION_ID = 1L;

    @Mock
    AccountRepo accountRepo;
    @Mock
    PersonRepo personRepo;
    @Mock
    OperationRepo operationRepo;
    @Mock
    JsonChangeBalanceProducer changeBalanceProducer;
    @Mock
    ConfirmProducer confirmProducer;

    @InjectMocks
    PaymentsService paymentsService;


    @Test
    public void saveNewPersonTest() {
        Person testPerson = getTestPerson(PERSON_ID);
        when(personRepo.save(testPerson)).thenReturn(testPerson);

        Person savedPerson = paymentsService.saveNewPerson(testPerson);
        verify(personRepo).save(testPerson);
        assertEquals(savedPerson, testPerson);
    }

    @Test
    public void saveNewAccountTest() {
        Person testPerson = getTestPerson(PERSON_ID);
        Account testAccount = getTestAccount(ACCOUNT_ID, PERSON_ID);
        when(personRepo.findById(PERSON_ID)).thenReturn(Optional.of(testPerson));
        when(accountRepo.save(testAccount)).thenReturn(testAccount);

        Account savedAccount = paymentsService.saveNewAccount(testAccount);
        verify(accountRepo).save(testAccount);
        assertEquals(savedAccount, testAccount);
    }

    @Test
    public void saveNewAccountWithWrongPersonIdTest() {
        Account testAccount = getTestAccount(ACCOUNT_ID, PERSON_ID);
        when(personRepo.findById(PERSON_ID)).thenReturn(Optional.empty());

        assertThrows(NoSuchPersonException.class, () -> paymentsService.saveNewAccount(testAccount));
    }

    @Test
    public void sendValidPaymentsTest() {
        Account testAccount = getTestAccount(ACCOUNT_ID, PERSON_ID);
        Operation operation = getTestOperation(OPERATION_ID, ACCOUNT_ID, false);
        LinkedList<Operation> operations = new LinkedList<>();
        operations.add(operation);
        when(accountRepo.findById(ACCOUNT_ID)).thenReturn(Optional.of(testAccount));

        paymentsService.sendPayments(operations);
        verify(changeBalanceProducer).send(operations);
    }

    @Test
    public void sendInvalidPaymentsTest() {
        Operation operation = getTestOperation(OPERATION_ID, ACCOUNT_ID, false);
        LinkedList<Operation> operations = new LinkedList<>();
        operations.add(operation);
        when(accountRepo.findById(ACCOUNT_ID)).thenReturn(Optional.empty());

        assertThrows(NoSuchAccountException.class, () -> paymentsService.sendPayments(operations));
    }

    @Test
    public void sendValidConfirmationTest() {
        Operation testOperation = getTestOperation(OPERATION_ID, ACCOUNT_ID, false);
        LinkedList<Integer> operations = new LinkedList<>();
        operations.add(Math.toIntExact(OPERATION_ID));
        when(operationRepo.findById(OPERATION_ID)).thenReturn(Optional.of(testOperation));

        paymentsService.sendConfirmation(operations);
        verify(confirmProducer).send(operations);
    }

    @Test
    public void sendAlreadyConfirmedOperationConfirmationTest() {
        Operation testOperation = getTestOperation(OPERATION_ID, ACCOUNT_ID, true);
        LinkedList<Integer> operations = new LinkedList<>();
        operations.add(Math.toIntExact(OPERATION_ID));
        when(operationRepo.findById(OPERATION_ID)).thenReturn(Optional.of(testOperation));

        assertThrows(AlreadyConfirmedOperationException.class, () -> paymentsService.sendConfirmation(operations));
    }

    @Test
    public void sendInvalidConfirmationsTest() {
        LinkedList<Integer> operations = new LinkedList<>();
        operations.add(Math.toIntExact(OPERATION_ID));
        when(operationRepo.findById(OPERATION_ID)).thenReturn(Optional.empty());

        assertThrows(NoSuchOperationException.class, () -> paymentsService.sendConfirmation(operations));
    }


    public static Person getTestPerson(Long id) {
        return Person.builder()
                .id(id)
                .firstName("test_firstName")
                .lastName("test_lastName")
                .build();
    }

    public static Account getTestAccount(Long id, Long personId) {
        return Account.builder()
                .id(id)
                .balance(BigDecimal.valueOf(500.0))
                .personId(personId)
                .build();
    }

    public static Operation getTestOperation(Long id, Long accountId, boolean isConfirmed) {
        return Operation.builder()
                .id(id)
                .accountId(accountId)
                .operationType(OperType.REFUND)
                .amount(BigDecimal.valueOf(50))
                .isConfirmed(isConfirmed)
                .build();
    }

}
