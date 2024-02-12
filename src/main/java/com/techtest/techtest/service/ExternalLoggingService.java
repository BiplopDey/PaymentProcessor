package com.techtest.techtest.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.techtest.techtest.service.exception.PaymentProcessingException;
import com.techtest.techtest.RestCallUtil;
import lombok.Builder;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ExternalLoggingService {
    @Value("${loggingUrl}")
    private String url;
    private final Logger logger = LoggerFactory.getLogger(ExternalLoggingService.class);
    @Autowired
    private RestCallUtil rest;


    public void logError(PaymentProcessingException exception) {
        logger.error(exception.getMessage());
        var errorModel = ErrorModel.builder()
                .errorDescription(exception.getErrorDescription())
                .errorType(exception.getError())
                .paymentId(exception.getPaymentId())
                .build();
        try {
            rest.post(url, errorModel);
        } catch (Exception e) {
            logger.error("Error at sending logs, message: " + e.getMessage());
        }
    }

    @Builder
    @Getter
    public static class ErrorModel {
        @JsonProperty("payment_id")
        private String paymentId;

        @JsonProperty("error_type")
        private String errorType;

        @JsonProperty("error_description")
        private String errorDescription;
    }
}
