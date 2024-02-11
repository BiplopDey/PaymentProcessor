package com.techtest.techtest.repository;

import com.techtest.techtest.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PaymentRepository extends JpaRepository<Payment, String> {
}
