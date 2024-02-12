package com.techtest.techtest.integration;

import com.techtest.techtest.PaymentEvent;
import com.techtest.techtest.repository.PaymentRepository;
import com.techtest.techtest.service.ExternalLoggingService;
import com.techtest.techtest.service.PaymentService;
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
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PaymentServiceIntegrationTest {

    @Autowired
    PaymentService paymentService;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    public void consume_offline_valid_payment_event() {
        PaymentEvent event = getValidOfflinePayment(UUID.randomUUID().toString());

        paymentService.process(event);

        Assert.isTrue(paymentRepository.findById(event.getPayment_id()).isPresent(), "s");
    }

    @Test
    public void consume_online_valid_payment_event() {
        PaymentEvent event = getFailedNetworkValidation();
        event.setPayment_id(UUID.randomUUID().toString());

        paymentService.process(event);

        Assert.isTrue(paymentRepository.findById(event.getPayment_id()).isPresent(), "s");
    }

    @Test
    public void consume_online_payment_reject_external_validation() {
        PaymentEvent event = getFailedNetworkValidation();

        paymentService.process(event);

        Assert.isTrue(paymentRepository.findById(event.getPayment_id()).isEmpty(), "s");
        assertItsLogged(event.getPayment_id());
    }


    private PaymentEvent getValidOnlinePayment(){
        PaymentEvent event = new PaymentEvent();
        event.setPayment_id("a629717b-f2cb-427f-8739-10251c029f42");
        event.setPayment_type("online");
        event.setAccount_id(1560);
        event.setCredit_card("4573145229078665562");
        event.setAmount(BigDecimal.valueOf(38));
        return event;
    }

    private PaymentEvent getValidOfflinePayment(String id) {
        PaymentEvent event = new PaymentEvent();
        event.setPayment_id(id);
        event.setPayment_type("offline");
        event.setAccount_id(872);
        event.setAmount(BigDecimal.valueOf(9));
        return event;
    }

    private PaymentEvent getFailedNetworkValidation(){
        PaymentEvent event = new PaymentEvent();
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
                }
        );

        List<ExternalLoggingService.ErrorModel> logs = response.getBody();
        boolean isLogged = logs.stream()
                .anyMatch(log -> id.equals(log.getPaymentId()));
        Assert.isTrue(isLogged, "Log entry for payment_id " + id + " not found");

    }

}
