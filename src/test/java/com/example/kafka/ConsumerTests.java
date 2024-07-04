package com.example.kafka;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.example.kafka.entity.Operation;
import com.example.kafka.kafka.simple.DlqConsumer;
import com.example.kafka.kafka.simple.RetryConsumer;
import com.example.kafka.kafka.simple.SuccessPaymentsConsumer;
import com.example.kafka.kafka.simple.ThrowErrorConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;


import static com.example.kafka.config.KafkaTopicConfig.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = { "my-retry", "dlg-succeed", "dlg-failed", "dlq" })
@DirtiesContext
@EnableKafka
public class ConsumerTests {

    @Autowired
    private KafkaTemplate<Long, Operation> kafkaTemplate = mock(KafkaTemplate.class);

    private TestAppender testAppender;
    private Operation testOperation;

    @BeforeEach
    public void setUp() {
        testAppender = new TestAppender();


        testOperation = PaymentsServiceTests.getTestOperation(1L, 1L, false);
    }

    @Test
    public void testThrowErrorConsumer() throws Exception {

        configureLogger(ThrowErrorConsumer.class);
        kafkaTemplate.send("dlg-failed", 1L, testOperation);

        Thread.sleep(5000);

        assertThat(testAppender.getLogEvents().stream().anyMatch(line -> line.getFormattedMessage().contains("Topic: \"dlg-failed\". Consumed new ERROR MESSAGE"))).isTrue();
        assertThat(testAppender.getLogEvents().stream().anyMatch(line -> line.getFormattedMessage().contains("Operation: " + testOperation.getId() + ": " + testOperation.getOperationType() + ". There are not enough funds in the account."))).isTrue();
    }

    @Test
    public void testSuccessPaymentsConsumer() throws Exception {

        configureLogger(SuccessPaymentsConsumer.class);
        kafkaTemplate.send("dlg-succeed", 1L, testOperation);

        Thread.sleep(5000);

        assertThat(testAppender.getLogEvents().stream().anyMatch(line -> line.getFormattedMessage().contains("Topic: \"dlg-succeed\". Consumed new SUCCESS MESSAGE"))).isTrue();
        assertThat(testAppender.getLogEvents().stream().anyMatch(line -> line.getFormattedMessage().contains("Operation " + testOperation.getId() + ": " + testOperation.getOperationType() + " " + testOperation.getAmount() + "."))).isTrue();
    }

    @Test
    public void testDlqConsumer() throws Exception {
        configureLogger(DlqConsumer.class);
        kafkaTemplate.send("dlq", 1L, testOperation);

        Thread.sleep(5000);

        assertThat(testAppender.getLogEvents().stream().anyMatch(line -> line.getFormattedMessage().contains("Topic: \"dlq\""))).isTrue();
        assertThat(testAppender.getLogEvents().stream().anyMatch(line -> line.getFormattedMessage().contains("Couldn't process Operation: " + testOperation.toString()))).isTrue();

    }

    // NOT WORKING!!!
    @Test
    public void testRetryConsumer() throws Exception {
        configureLogger(RetryConsumer.class);
        kafkaTemplate.send("my-retry", 1L, testOperation);

        Thread.sleep(5000);

        assertThat(testAppender.getLogEvents().stream().anyMatch(line -> line.getFormattedMessage().contains("Topic: \"my-retry\""))).isTrue();
        assertThat(testAppender.getLogEvents().stream().anyMatch(line -> line.getFormattedMessage().contains("Trying to process Operation: " + testOperation.toString()))).isTrue();

        verify(kafkaTemplate).send(CHANGE_BALANCE, testOperation.getAccountId(), testOperation);
        verify(kafkaTemplate).send(CONFIRMATION, testOperation.getId(), testOperation);
//        Thread.sleep(60000);

        verify(kafkaTemplate).send(DLQ, testOperation);
    }

    private void configureLogger(Class<?> clazz) {
        Logger logger = (Logger) LoggerFactory.getLogger(clazz);
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        testAppender.setContext(loggerContext);
        testAppender.start();
        logger.addAppender(testAppender);
        logger.setLevel(Level.INFO);
    }

}
