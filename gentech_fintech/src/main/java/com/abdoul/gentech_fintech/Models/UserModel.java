package com.abdoul.gentech_fintech.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Table(name = "users", indexes ={
        @Index(columnList = "email"),
        @Index(columnList = "phone")
})
@Getter
@Setter
@NoArgsConstructor
public class UserModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    private String email;

    private String phone;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean deleted = false;

    @Column(nullable = false)
    private boolean flagged = false;

    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime createdAt;

    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime updatedAt;

    private String role = "users";

    @PrePersist
    private void onCreate (){
        this.createdAt = ZonedDateTime.now(ZoneId.of("UTC"));
    }

    @PreUpdate
    private void onUpdate(){
        this.updatedAt = ZonedDateTime.now(ZoneId.of("UTC"));
    }

    @OneToMany(mappedBy = "user")
    private List<AuditLogs> logs;

    private boolean faEnabled = true;

    @OneToMany(mappedBy = "user")
    private List<RefreshModel> refresh;

    @OneToMany(mappedBy = "user")
    private List<KycModel> kyc;

    @OneToOne(mappedBy = "user")
    private TwoFactorModel twoFactor;

    @OneToOne(mappedBy = "user")
    private WalletModel wallet;

}
