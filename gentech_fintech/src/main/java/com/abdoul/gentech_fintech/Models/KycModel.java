package com.abdoul.gentech_fintech.Models;

import com.abdoul.gentech_fintech.Configuration.KycStatus;
import com.abdoul.gentech_fintech.Configuration.KycType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Table(name = "kycs")
@Getter
@Setter
@NoArgsConstructor
public class KycModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String url;

    @Enumerated(EnumType.STRING)
    private KycType kycType;

    @Enumerated(EnumType.STRING)
    private KycStatus status = KycStatus.Pending;

    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime submittedAt;

    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime reviewedAt;

    @ManyToOne
    @JoinColumn(name = "kyc_user", unique = true)
    private UserModel user;

    @PrePersist
    protected void onCreate() {
        this.submittedAt = ZonedDateTime.now(ZoneId.of("UTC"));
    }
}

