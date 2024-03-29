package com.techtest.techtest.consumer;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
public class PaymentEvent {
    private String payment_id;
    private Integer account_id;
    private String payment_type;
    private String credit_card;
    private BigDecimal amount;
    private long delay;
}
