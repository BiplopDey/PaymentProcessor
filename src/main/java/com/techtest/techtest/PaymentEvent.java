package com.techtest.techtest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentEvent {
    private String payment_id;
    private int account_id;
    private String payment_type;
    private String credit_card;
    private double amount;
    private int delay;
}
