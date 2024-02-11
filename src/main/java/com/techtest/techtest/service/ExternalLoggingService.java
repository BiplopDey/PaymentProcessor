package com.techtest.techtest.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.techtest.techtest.PaymentProcessingException;
import com.techtest.techtest.RestCallUtil;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExternalLoggingService {
    private final Logger logger = LoggerFactory.getLogger(ExternalLoggingService.class);
    @Autowired
    private RestCallUtil rest;

    public void logError(PaymentProcessingException exception) {
        String url = "http://localhost:9000/log";
        var errorModel = ErrorModel.builder()
                .errorDescription(exception.getErrorDescription())
                .errorType(exception.getError())
                .paymentId(exception.getPaymentId())
                .build();
        try {
            rest.post(url, errorModel);
        } catch (Exception e){
            logger.error("Error at sending logs, message: " + e.getMessage());
        }
    }

    @Builder
    private static class ErrorModel{
        @JsonProperty("payment_id")
        private String paymentId;

        @JsonProperty("error_type")
        private String errorType;

        @JsonProperty("error_description")
        private String errorDescription;
    }
}
