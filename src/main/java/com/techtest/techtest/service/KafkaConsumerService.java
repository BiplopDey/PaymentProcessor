package com.techtest.techtest.service;

import com.techtest.techtest.PaymentEvent;
import com.techtest.techtest.PaymentProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
public class KafkaConsumerService {
    private final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);

    @Autowired
    PaymentService paymentService;
    @Autowired
    ExternalLoggingService externalLoggingService;

    @KafkaListener(topics = {"online", "offline"}, groupId = "group_id", containerFactory = "kafkaListenerContainerFactory")
    public void consume(PaymentEvent event) {

        try {
            validate(event);
            logger.info(event.toString());
            paymentService.process(event);
        } catch (PaymentProcessingException e) {
            e.setPaymentId(event.getPayment_id());
            logger.error(e.getMessage());
            externalLoggingService.logError(e);
        } catch (Exception e){
            var error = PaymentProcessingException.otherTypeError(event.getPayment_id(), "Internal unhandled exception: " + e.getMessage());
            logger.error(error.getMessage());
            externalLoggingService.logError(error);
        }
    }

    private void validate(PaymentEvent event) throws Exception {
        boolean isValid = nonNull(event.getPayment_id())
                && nonNull(event.getAccount_id())
                && nonNull(event.getPayment_type())
                && nonNull(event.getAmount())
                && List.of("online", "offline").contains(event.getPayment_type());
        var error = PaymentProcessingException.otherTypeError(event.getPayment_id(), "Invalid payment event");
        if (!isValid) {
            throw error;
        }
        if (event.getPayment_type().equals("online") && isNull(event.getCredit_card())) {
            throw error;
        }
    }
}
