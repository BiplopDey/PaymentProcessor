package com.techtest.techtest.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;


@Entity
@Table(name = "payments")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @Column(name = "payment_id", length = 100)
    private String paymentId;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "payment_type", length = 150, nullable = false)
    private String paymentType;

    @Column(name = "credit_card", length = 100)
    private String creditCard;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "created_on", insertable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;

}