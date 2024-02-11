package com.techtest.techtest.service;

import com.techtest.techtest.OnlinePaymentValidator;
import com.techtest.techtest.PaymentEvent;
import com.techtest.techtest.model.Account;
import com.techtest.techtest.model.Payment;
import com.techtest.techtest.repository.AccountRepository;
import com.techtest.techtest.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private OnlinePaymentValidator validator;

    public void process(PaymentEvent paymentEvent){
        var account = updateLastPaymentDateAndGet(paymentEvent.getAccount_id());
        var payment = map(paymentEvent, account);
        if(payment.getPaymentType().equals("offline")){//TODO: use enum
            paymentRepository.save(payment);
        }
        if(payment.getPaymentType().equals("online")){
            if(validator.validate(paymentEvent)){
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

    private Account updateLastPaymentDateAndGet(Integer accountId) {
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            account.setLastPaymentDate(new Date());
            return accountRepository.save(account);
        }
        return null;
    }
}
