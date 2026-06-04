package com.abdoul.gentech_fintech.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
public class AuditLogs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime initiatedAt;

    @PrePersist
    protected void onCreate (){
        this.initiatedAt = ZonedDateTime.now(ZoneId.of("UTC"));
    }

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "logs_user")
    private UserModel user;

    private String country;

    private String city;

    private String latitude;

    private String longitude;

    private String device;

    private String os;

    private String browser;
}
