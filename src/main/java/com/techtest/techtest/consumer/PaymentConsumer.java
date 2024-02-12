package com.techtest.techtest.consumer;

import com.techtest.techtest.PaymentEvent;
import com.techtest.techtest.service.ExternalLoggingService;
import com.techtest.techtest.service.PaymentService;
import com.techtest.techtest.service.exception.PaymentProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
public class PaymentConsumer {
    private final Logger logger = LoggerFactory.getLogger(PaymentConsumer.class);

    @Autowired
    private PaymentService paymentService;
    @Autowired
    private ExternalLoggingService externalLoggingService;

    @KafkaListener(topics = {"online", "offline"}, groupId = "group_id", containerFactory = "kafkaListenerContainerFactory")
    public void consume(PaymentEvent event) {
        logger.info(event.toString());
        if (!isValid(event)) {
            externalLoggingService.logError(PaymentProcessingException.otherTypeError(event.getPayment_id(), "Invalid payment event"));
            return;
        }
        paymentService.process(event);
    }

    private boolean isValid(PaymentEvent event) {
        boolean isValid = nonNull(event.getPayment_id())
                && nonNull(event.getAccount_id())
                && nonNull(event.getPayment_type())
                && nonNull(event.getAmount())
                && List.of("online", "offline").contains(event.getPayment_type());
        if (!isValid) {
            return false;
        }
        if (event.getPayment_type().equals("online") && isNull(event.getCredit_card())) {
            return false;
        }
        return true;
    }
}
