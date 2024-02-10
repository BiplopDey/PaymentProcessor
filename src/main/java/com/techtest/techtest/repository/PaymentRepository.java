package com.techtest.techtest.repository;

import com.techtest.techtest.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


public interface PaymentRepository extends JpaRepository<Payment, String> {
}
