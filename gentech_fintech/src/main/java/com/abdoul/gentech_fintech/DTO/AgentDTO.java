package com.abdoul.gentech_fintech.DTO;

import com.abdoul.gentech_fintech.Configuration.KycStatus;
import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NonNull;
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
    public static class DepositWith{
        @DecimalMin(value = "0.1", message = "Amount must greater than or equal to 0.1")
        @DecimalMax(value = "10000", message = "Amount must be less than 10000")
        @Digits(fraction = 2, integer = 5)
        private BigDecimal amount;

        @NotBlank(message = "Description field can't be blank")
        private String description;

        @NotNull(message = "Enter the wallet id")
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

    @Getter
    @Setter
    public static class Flag{
        private Long id;
        private String email;
        private String phone;
    }

    @Getter
    @Setter
    public static class Kyc{
        @NotNull(message = "Id is required")
        private Long id;
        @NonNull
        private KycStatus status;
        @NotBlank(message = "Enter the reason why you made this choice")
        private String reason;
    }

    @Getter
    @Setter
    public static class Url{
        @NotNull(message = "Id is required")
        private Long id;
    }
}
