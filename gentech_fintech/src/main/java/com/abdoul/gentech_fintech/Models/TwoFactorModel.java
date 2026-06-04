package com.abdoul.gentech_fintech.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Table(name = "two_factors")
@Getter
@Setter
@NoArgsConstructor
public class TwoFactorModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    private String code;

    private boolean revoked = false;

    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime createdAt;

    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime expiresAt;

    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime updatedAt;

    @PrePersist
    public void onCreate(){
        this.createdAt = ZonedDateTime.now(ZoneId.of("UTC"));
        this.expiresAt = ZonedDateTime.now(ZoneId.of("UTC")).plusMinutes(10);
    }

    @PreUpdate
    public void onUpdate(){
        this.updatedAt = ZonedDateTime.now(ZoneId.of("UTC"));
        this.expiresAt = ZonedDateTime.now(ZoneId.of("UTC")).plusMinutes(10);
        this.revoked = false;
    }

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "two_factor_user")
    private UserModel user;
}
