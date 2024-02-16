package com.techtest.techtest.integration;

import com.techtest.techtest.consumer.PaymentEvent;
import com.techtest.techtest.consumer.PaymentConsumer;
import com.techtest.techtest.model.Account;
import com.techtest.techtest.repository.AccountRepository;
import com.techtest.techtest.repository.PaymentRepository;
import com.techtest.techtest.service.ExternalLoggingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PaymentConsumerIntegrationTest {

    @Autowired
    private PaymentConsumer paymentConsumer;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    public void databaseSetup() {
        paymentRepository.deleteAll();

        List<Account> accounts = accountRepository.findAll();
        accounts.forEach(account -> account.setLastPaymentDate(null));
        accountRepository.saveAll(accounts);
    }

    @Test
    public void consume_invalid_payment_event() {
        var event = getValidOfflinePayment();
        event.setAmount(null);

        paymentConsumer.consume(event);

        assertTrue(paymentRepository.findById(event.getPayment_id()).isEmpty());
        assertItsLogged(event.getPayment_id());
    }


    @Test
    public void consume_offline_valid_payment_event() {
        var event = getValidOfflinePayment();

        paymentConsumer.consume(event);

        assertTrue(paymentRepository.findById(event.getPayment_id()).isPresent());
        var account = accountRepository.findById(event.getAccount_id()).get();
        assertTrue(Objects.nonNull(account.getLastPaymentDate()));
    }

    @Test
    public void consume_online_valid_payment_event() {
        var event = getValidOnlinePayment();

        paymentConsumer.consume(event);

        assertTrue(paymentRepository.findById(event.getPayment_id()).isPresent());
    }

    @Test
    public void consume_online_payment_reject_external_validation() {
        var event = getFailedNetworkValidation();

        paymentConsumer.consume(event);

        assertTrue(paymentRepository.findById(event.getPayment_id()).isEmpty());
        assertItsLogged(event.getPayment_id());
        var account = accountRepository.findById(event.getAccount_id()).get();
        assertTrue(Objects.isNull(account.getLastPaymentDate()));
    }


    private PaymentEvent getValidOnlinePayment() {
        var event = getFailedNetworkValidation();
        event.setPayment_id(UUID.randomUUID().toString());
        return event;
    }

    private PaymentEvent getValidOfflinePayment() {
        var event = new PaymentEvent();
        event.setPayment_id(UUID.randomUUID().toString());
        event.setPayment_type("offline");
        event.setAccount_id(872);
        event.setAmount(BigDecimal.valueOf(9));
        return event;
    }

    private PaymentEvent getFailedNetworkValidation() {
        var event = new PaymentEvent();
        event.setPayment_id("d0379b71-bd43-454d-9b98-0927cd189bc8");
        event.setPayment_type("online");
        event.setAccount_id(1561);
        event.setCredit_card("4068529176239185014");
        event.setAmount(BigDecimal.valueOf(41));
        return event;
    }

    private void assertItsLogged(String id) {
        ResponseEntity<List<ExternalLoggingService.ErrorModel>> response = restTemplate.exchange(
                "http://localhost:9000/logs",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ExternalLoggingService.ErrorModel>>() {
                });

        List<ExternalLoggingService.ErrorModel> logs = response.getBody();
        boolean isLogged = logs.stream()
                .anyMatch(log -> id.equals(log.getPaymentId()));
        Assert.isTrue(isLogged, "Log entry for payment_id " + id + " not found");

    }

}
