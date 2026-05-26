package com.abdoul.gentech_fintech.Models;

import jakarta.persistence.*;

@Entity
@Table(name = "users", indexes ={
        @Index(columnList = "email"),
        @Index(columnList = "phone")
})
public class UserModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    private String email;

    private String phone;

    private String password;

    private String name;

    private boolean deleted = false;

    private boolean flagged = false;

}
