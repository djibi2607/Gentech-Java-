package com.abdoul.gentech_fintech.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Table(name = "wallets", indexes = {
        @Index(columnList = "wallet_user")
})
@Getter
@Setter
@NoArgsConstructor
public class WalletModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wallet_id")
    private Long id;

    @Column(precision = 29, scale = 2, nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @OneToOne
    @JoinColumn(name = "wallet_user", nullable = false)
    private UserModel user;

    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE", nullable = false)
    private ZonedDateTime createdAt;

    @PrePersist
    private void onCreate (){
        this.createdAt = ZonedDateTime.now(ZoneId.of("UTC"));
    }

    @OneToMany(mappedBy = "senderWallet")
    private List<TransactionModel> transactionsSent;

    @OneToMany(mappedBy = "receiverWallet")
    private List<TransactionModel> transactionsReceived;
}
