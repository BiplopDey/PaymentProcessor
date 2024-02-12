package com.techtest.techtest.service;

import com.techtest.techtest.PaymentEvent;
import com.techtest.techtest.PaymentProcessingException;
import com.techtest.techtest.model.Account;
import com.techtest.techtest.model.Payment;
import com.techtest.techtest.repository.AccountRepository;
import com.techtest.techtest.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class PaymentService {
    private final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private OnlinePaymentValidatorService validator;
    @Autowired
    private ExternalLoggingService externalLoggingService;

    public void process(PaymentEvent event){
        try {
            var account = updateLastPaymentDateAndGet(event.getAccount_id());
            var payment = map(event, account);
            processPayment(payment);
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

    public void processPayment(Payment payment) throws Exception {
        if(payment.getPaymentType().equals("offline")){//TODO: use enum
            paymentRepository.save(payment);
        }
        if(payment.getPaymentType().equals("online")){
            if(validator.validate(payment)){
                paymentRepository.save(payment);
            }
        }
    }

    private static Payment map(PaymentEvent paymentEvent, Account account) {
        return Payment.builder()
                .paymentId(paymentEvent.getPayment_id())
                .paymentType(paymentEvent.getPayment_type())
                .account(account)
                .creditCard(paymentEvent.getCredit_card())
                .amount(paymentEvent.getAmount())
                .build();
    }

    private Account updateLastPaymentDateAndGet(Integer accountId) throws Exception {
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isEmpty()) {
            throw PaymentProcessingException.databaseTypeError(null, "account with id: " + accountId+ " not found");
        }
        Account account = accountOpt.get();
        account.setLastPaymentDate(new Date());
        return accountRepository.save(account);
    }
}
