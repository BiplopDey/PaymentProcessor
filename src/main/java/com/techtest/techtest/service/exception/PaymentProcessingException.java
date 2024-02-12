package com.techtest.techtest.service.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@AllArgsConstructor
@Getter
public class PaymentProcessingException extends Exception {
    @Setter
    private String paymentId;
    private String error;
    private String errorDescription;

    public static PaymentProcessingException databaseTypeError(String paymentId, String errorDescription) {
        return new PaymentProcessingException(paymentId, "database", errorDescription);
    }

    public static PaymentProcessingException networkTypeError(String paymentId, String errorDescription) {
        return new PaymentProcessingException(paymentId, "network", errorDescription);
    }

    public static PaymentProcessingException otherTypeError(String paymentId, String errorDescription) {
        return new PaymentProcessingException(paymentId, "other", errorDescription);
    }

    @Override
    public String getMessage() {
        return String.format("Error processing payment [%s]: %s - %s", paymentId, error, errorDescription);
    }

}
