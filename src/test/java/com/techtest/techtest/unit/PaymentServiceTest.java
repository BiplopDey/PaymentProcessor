package com.techtest.techtest.unit;

import com.techtest.techtest.PaymentEvent;
import com.techtest.techtest.PaymentProcessingException;
import com.techtest.techtest.model.Account;
import com.techtest.techtest.model.Payment;
import com.techtest.techtest.repository.AccountRepository;
import com.techtest.techtest.repository.PaymentRepository;
import com.techtest.techtest.service.ExternalLoggingService;
import com.techtest.techtest.service.OnlinePaymentValidatorService;
import com.techtest.techtest.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private OnlinePaymentValidatorService validator;

    @Mock
    private ExternalLoggingService externalLoggingService;

    @InjectMocks
    private PaymentService paymentService;

    private PaymentEvent paymentEvent;
    private Account account;

    @BeforeEach
    void setUp() {
        paymentEvent = new PaymentEvent();
        account = new Account();
    }


    @Test
    void test_process_with_payment_processing_exception() {
        when(accountRepository.findById(anyInt())).thenThrow(new RuntimeException("Test Exception"));

        paymentService.process(paymentEvent);

        verify(externalLoggingService, times(1)).logError(any(PaymentProcessingException.class));
    }

    @Test
    void test_process_with_unknown_exception() {
        when(accountRepository.findById(anyInt())).thenThrow(new RuntimeException("Test Exception"));

        paymentService.process(paymentEvent);

        verify(externalLoggingService, times(1)).logError(any(PaymentProcessingException.class));
    }

    @Test
    void test_process_payment_when_offline() throws Exception {
        Payment payment = new Payment();
        payment.setPaymentType("offline");

        paymentService.processPayment(payment);

        verify(paymentRepository).save(payment);
        verifyNoInteractions(validator);
    }

    @Test
    void test_process_payment_when_online_and_valid() throws Exception {
        Payment payment = new Payment();
        payment.setPaymentType("online");
        when(validator.validate(payment)).thenReturn(true);

        paymentService.processPayment(payment);

        verify(paymentRepository).save(payment);
        verify(validator).validate(payment);
    }

    @Test
    void test_process_payment_when_online_and_invalid() throws Exception {
        Payment payment = new Payment();
        payment.setPaymentType("online");
        when(validator.validate(payment)).thenReturn(false);

        paymentService.processPayment(payment);

        verifyNoInteractions(paymentRepository);
        verify(validator).validate(payment);
    }

}