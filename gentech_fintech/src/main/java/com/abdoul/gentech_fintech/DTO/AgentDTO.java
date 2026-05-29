package com.abdoul.gentech_fintech.DTO;

import jakarta.persistence.Column;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

public class AgentDTO {
    @Getter
    @Setter
    public static class UserCredentials{
        private String email;
        private String phone;
    }

    @Getter
    @Setter
    public static class Deposit{
        @DecimalMin(value = "0.1", message = "Amount must greater than or equal to 0.1")
        @DecimalMax(value = "10000", message = "Amount must be less than 10000")
        @Digits(fraction = 2, integer = 5)
        private BigDecimal amount;

        @NotBlank(message = "Description field can't be blank")
        private String description;

        @NotBlank(message = "Enter the wallet id")
        private Long walletId;
    }

    @Getter
    @Setter
    public static class DepositWith2fa{
        @NotBlank(message = "Enter the token")
        private String token;

        @DecimalMin(value = "0.1", message = "Amount must greater than or equal to 0.1")
        @DecimalMax(value = "10000", message = "Amount must be less than 10000")
        @Digits(fraction = 2, integer = 5)
        private BigDecimal amount;

        @NotBlank(message = "Description field can't be blank")
        private String description;

        @NotBlank
        private String code;
    }
}
