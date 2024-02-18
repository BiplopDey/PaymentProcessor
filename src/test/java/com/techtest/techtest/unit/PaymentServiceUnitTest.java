package com.techtest.techtest.unit;

import com.techtest.techtest.model.Account;
import com.techtest.techtest.model.Payment;
import com.techtest.techtest.model.PaymentType;
import com.techtest.techtest.repository.AccountRepository;
import com.techtest.techtest.repository.PaymentRepository;
import com.techtest.techtest.service.ExternalLoggingService;
import com.techtest.techtest.service.OnlinePaymentValidatorService;
import com.techtest.techtest.service.PaymentService;
import com.techtest.techtest.service.exception.PaymentProcessingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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

    @Captor
    private ArgumentCaptor<Payment> paymentCaptor;

    @Test
    void test_process_with_payment_processing_exception() {
        var payment = paymentWithAccount();
        payment.setPaymentType(PaymentType.OFFLINE);
        when(accountRepository.findById(anyInt()))
                .thenThrow(new RuntimeException("Test Exception"));

        paymentService.process(payment);

        verify(externalLoggingService, times(1))
                .logError(any(PaymentProcessingException.class));
    }

    @Test
    void test_process_when_offline() {
        var payment = paymentWithAccount();
        payment.setPaymentType(PaymentType.OFFLINE);
        when(accountRepository.findById(anyInt()))
                .thenReturn(Optional.of(payment.getAccount()));
        when(accountRepository.save(any())).thenReturn(payment.getAccount());

        paymentService.process(payment);

        verify(paymentRepository).save(paymentCaptor.capture());
        Payment capturedPayment = paymentCaptor.getValue();
        Assertions.assertTrue(Objects.nonNull(capturedPayment.getAccount().getLastPaymentDate()));
        verifyNoInteractions(validator);
    }

    @Test
    void test_process_payment_when_online_and_invalid() throws Exception {
        var payment = paymentWithAccount();
        payment.setPaymentType(PaymentType.ONLINE);
        when(validator.validate(payment)).thenReturn(false);

        paymentService.process(payment);

        verifyNoInteractions(paymentRepository);
        verify(accountRepository, never()).save(any());
    }

    private Payment paymentWithAccount() {
        var account = new Account();
        account.setAccountId(1);
        var payment = new Payment();
        payment.setAccount(account);
        return payment;
    }

}