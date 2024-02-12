package com.techtest.techtest.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.techtest.techtest.PaymentProcessingException;
import com.techtest.techtest.RestCallUtil;
import com.techtest.techtest.model.Payment;
import lombok.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class OnlinePaymentValidatorService {
    @Autowired
    private RestCallUtil rest;
    private final String url = "http://localhost:9000/payment"; //TODO: inject from properties

    public boolean validate(Payment payment) throws Exception {
        if(!payment.getPaymentType().equals("online")){
            throw new IllegalArgumentException("Payment should be type online for external validation");
        }
        try{
            return rest.post(url, map(payment)).isOk();
        } catch (Exception e){
            throw PaymentProcessingException.networkTypeError(payment.getPaymentId(), "network failed to validate. message:"+ e.getMessage());
        }
    }

    private PaymentRequest map(Payment payment) {
        return PaymentRequest.builder()
                .paymentId(payment.getPaymentId())
                .accountId(payment.getAccount().getAccountId())
                .creditCard(payment.getCreditCard())
                .amount(payment.getAmount())
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
