package com.abdoul.gentech_fintech.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
public class RefreshModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime createdAt;

    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime expiresAt;

    private boolean isRevoked = false;

    @ManyToOne
    @JoinColumn(name = "refresh_user")
    private UserModel user;

    @PrePersist
    protected void onCreate (){
        this.createdAt = ZonedDateTime.now(ZoneId.of("UTC"));
        this.expiresAt = ZonedDateTime.now(ZoneId.of("UTC")).plusHours(2);
    }
}
