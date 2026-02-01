package com.project.cinemabackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "payments", schema = "public")
public class Payment {
    @Id
    @Column(name = "payment_id", nullable = false)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Booking booking;


    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_method", nullable = false, length = 50)
    private String paymentMethod;

    @ColumnDefault("'pending'")
    @Column(name = "payment_status", nullable = false, length = 20)
    private String paymentStatus;

    @Column(name = "transaction_id")
    private String transactionId;

    private String provider; // payu

    @Column(name = "payment_url", length = 800)
    private String paymentUrl;

    @Column(name = "paid_at")
    private OffsetDateTime paidAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;
}