package com.techtest.techtest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Component
public class OnlinePaymentValidator {
    @Autowired
    private RestTemplate restTemplate;

    public boolean validate(PaymentEvent paymentEvent) {
        String url = "http://localhost:9000/payment"; //TODO: inject from properties
        ResponseEntity<String> response = post(url, map(paymentEvent));
        return response.getStatusCode().is2xxSuccessful();
    }

    private ResponseEntity<String> post(String url, PaymentRequest paymentRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PaymentRequest> entity = new HttpEntity<>(paymentRequest, headers);
        return  restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    }

    private PaymentRequest map(PaymentEvent paymentEvent) {
        return PaymentRequest.builder()
                .paymentId(paymentEvent.getPayment_id())
                .accountId(paymentEvent.getAccount_id())
                .creditCard(paymentEvent.getCredit_card())
                .amount(paymentEvent.getAmount())
                .build();
    }

    @Builder
    private static class PaymentRequest {
        @JsonProperty("payment_id")
        private String paymentId;

        @JsonProperty("account_id")
        private Integer accountId;

        @JsonProperty("payment_type")
        private String paymentType;

        @JsonProperty("credit_card")
        private String creditCard;

        @JsonProperty("amount")
        private BigDecimal amount;
    }
}
