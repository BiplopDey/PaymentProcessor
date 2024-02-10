package com.techtest.techtest;

import com.techtest.techtest.model.Payment;
import org.springframework.stereotype.Component;

@Component
public class OnlinePaymentValidator {
    public boolean validate(Payment payment){
        return false;
    }
}
