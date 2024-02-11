package com.techtest.techtest.integration;

import com.techtest.techtest.PaymentEvent;
import com.techtest.techtest.repository.PaymentRepository;
import com.techtest.techtest.service.PaymentConsumerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PaymentConsumerServiceIntegrationTest {

    @Autowired
    PaymentConsumerService kafkaConsumerService;

    @Autowired
    PaymentRepository paymentRepository;

    @Test
    public void consumeEvent(){
        PaymentEvent event = PaymentEvent.builder()
                .payment_id(UUID.randomUUID().toString())
                .account_id(872)
                .payment_type("offline")
                .amount(BigDecimal.valueOf(9))
                .build();

        kafkaConsumerService.consume(event);

        Assert.isTrue(paymentRepository.findById(event.getPayment_id()).isPresent(),"s");
    }
}
