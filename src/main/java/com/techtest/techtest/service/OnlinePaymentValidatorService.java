package com.techtest.techtest.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.techtest.techtest.PaymentEvent;
import com.techtest.techtest.PaymentProcessingException;
import com.techtest.techtest.RestCallUtil;
import lombok.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class OnlinePaymentValidatorService {
    @Autowired
    private RestCallUtil rest;

    public boolean validate(PaymentEvent paymentEvent) throws Exception {
        String url = "http://localhost:9000/payment"; //TODO: inject from properties
        try{
            ResponseEntity<String> response = rest.post(url, map(paymentEvent));
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e){
            throw PaymentProcessingException.networkTypeError(paymentEvent.getPayment_id(), "network failed to validate. message:"+ e.getMessage());
        }
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
