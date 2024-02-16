package com.techtest.techtest.service;

import com.techtest.techtest.consumer.PaymentEvent;
import com.techtest.techtest.service.exception.PaymentProcessingException;
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

    public void process(Payment payment) {
        try {
            processPayment(payment);
        } catch (PaymentProcessingException e) {
            externalLoggingService.logError(e);
        } catch (Exception e) {
            externalLoggingService.logError(
                    PaymentProcessingException.otherTypeError(payment.getPaymentId(),
                            "Internal unhandled exception: " + e.getMessage()));
        }
    }

    public void processPayment(Payment payment) throws Exception {
        switch (payment.getPaymentType()) {
            case OFFLINE -> updateAccountAndSavePayment(payment);
            case ONLINE -> {
                if (validator.validate(payment))
                    updateAccountAndSavePayment(payment);
            }
        }
    }

    private void updateAccountAndSavePayment(Payment payment) throws Exception {
        var account = updateAccountLastPaymentDate(payment);
        payment.setAccount(account);
        paymentRepository.save(payment);
    }

    private Account updateAccountLastPaymentDate(Payment payment) throws Exception {
        var account = accountRepository.findById(payment.getAccount().getAccountId())
                .orElseThrow(()->PaymentProcessingException.databaseTypeError(
                        payment.getPaymentId(),
                        "account with id: " + payment.getAccount().getAccountId() + " not found"));
        account.setLastPaymentDate(new Date());
        return accountRepository.save(account);
    }
}
