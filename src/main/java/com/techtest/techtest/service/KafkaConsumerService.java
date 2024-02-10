package com.techtest.techtest.service;

import com.techtest.techtest.PaymentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {
    Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);

    @Autowired
    PaymentService paymentService;

    @KafkaListener(topics = {"online", "offline"}, groupId = "group_id", containerFactory = "kafkaListenerContainerFactory")
    public void consume(PaymentEvent paymentEvent) {
       logger.info(paymentEvent.toString());
       paymentService.process(paymentEvent);
    }

}
