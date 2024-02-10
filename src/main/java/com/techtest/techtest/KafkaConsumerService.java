package com.techtest.techtest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {
    Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);

    @KafkaListener(topics = {"online", "offline"}, groupId = "group_id", containerFactory = "kafkaListenerContainerFactory")
    public void consume(PaymentEvent paymentEvent) {
       logger.info(paymentEvent.getPayment_type());
    }

}
