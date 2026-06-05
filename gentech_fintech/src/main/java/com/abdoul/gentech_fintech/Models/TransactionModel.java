package com.abdoul.gentech_fintech.Models;

import com.abdoul.gentech_fintech.Configuration.TransType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
public class TransactionModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    @Column(precision = 7, scale = 2, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private TransType transType;

    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = ZonedDateTime.now(ZoneId.of("UTC"));
    }

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "sender_wallet")
    private WalletModel senderWallet;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "receiver_wallet")
    private WalletModel receiverWallet;
}
