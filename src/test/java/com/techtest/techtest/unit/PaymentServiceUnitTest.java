package com.techtest.techtest.unit;

import com.techtest.techtest.PaymentEvent;
import com.techtest.techtest.service.exception.PaymentProcessingException;
import com.techtest.techtest.model.Account;
import com.techtest.techtest.repository.AccountRepository;
import com.techtest.techtest.repository.PaymentRepository;
import com.techtest.techtest.service.ExternalLoggingService;
import com.techtest.techtest.service.OnlinePaymentValidatorService;
import com.techtest.techtest.service.PaymentService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Objects;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceUnitTest {

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


    @Test
    void test_process_with_payment_processing_exception() {
        var paymentEvent = new PaymentEvent();
        when(accountRepository.findById(anyInt())).thenThrow(new RuntimeException("Test Exception"));

        paymentService.process(paymentEvent);

        verify(externalLoggingService, times(1)).logError(any(PaymentProcessingException.class));
    }

    @Test
    void test_process_with_unknown_exception() {
        var paymentEvent = new PaymentEvent();
        when(accountRepository.findById(anyInt())).thenThrow(new RuntimeException("Test Exception"));

        paymentService.process(paymentEvent);

        verify(externalLoggingService, times(1)).logError(any(PaymentProcessingException.class));
    }

    @Test
    void test_process_when_offline(){
        var account = new Account();
        var paymentEvent = new PaymentEvent();
        paymentEvent.setAccount_id(1);
        paymentEvent.setPayment_type("offline");
        when(accountRepository.findById(anyInt())).thenReturn(Optional.of(account));

        paymentService.process(paymentEvent);

        verify(paymentRepository).save(any());
        Assertions.assertTrue(Objects.nonNull(account.getLastPaymentDate()));
        verifyNoInteractions(validator);
    }

    @Test
    void test_process_payment_when_online_and_invalid() throws Exception {
        var account = new Account();
        var paymentEvent = new PaymentEvent();
        paymentEvent.setPayment_type("online");
        paymentEvent.setAccount_id(1);
        when(accountRepository.findById(anyInt())).thenReturn(Optional.of(account));
        when(validator.validate(any())).thenReturn(false);

        paymentService.process(paymentEvent);

        verifyNoInteractions(paymentRepository);
        verify(accountRepository, never()).save(any());
    }


}