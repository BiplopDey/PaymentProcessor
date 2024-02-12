package com.techtest.techtest.service;

import com.techtest.techtest.PaymentEvent;
import com.techtest.techtest.service.exception.PaymentProcessingException;
import com.techtest.techtest.model.PaymentType;
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

    public void process(PaymentEvent event) {
        try {
            var account = updateLastPaymentDateAndGet(event);
            var payment = map(event, account);
            processPayment(payment);
        } catch (PaymentProcessingException e) {
            externalLoggingService.logError(e);
        } catch (Exception e) {
            externalLoggingService.logError(
                    PaymentProcessingException.otherTypeError(event.getPayment_id(),
                            "Internal unhandled exception: " + e.getMessage()));
        }
    }

    public void processPayment(Payment payment) throws Exception {
        switch (payment.getPaymentType()) {
            case OFFLINE -> save(payment);
            case ONLINE -> {
                if (validator.validate(payment))
                    save(payment);
            }
        }
    }

    private void save(Payment payment) {
        accountRepository.save(payment.getAccount());
        paymentRepository.save(payment);
    }

    private static Payment map(PaymentEvent paymentEvent, Account account) {
        return Payment.builder()
                .paymentId(paymentEvent.getPayment_id())
                .paymentType(PaymentType.valueOf(paymentEvent.getPayment_type().toUpperCase()))
                .account(account)
                .creditCard(paymentEvent.getCredit_card())
                .amount(paymentEvent.getAmount())
                .build();
    }

    private Account updateLastPaymentDateAndGet(PaymentEvent event) throws Exception {
        Optional<Account> accountOpt = accountRepository.findById(event.getAccount_id());
        if (accountOpt.isEmpty()) {
            throw PaymentProcessingException.databaseTypeError(event.getPayment_id(), "account with id: " + event.getAccount_id() + " not found");
        }
        Account account = accountOpt.get();
        account.setLastPaymentDate(new Date());
        return account;
    }
}
