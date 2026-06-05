package com.abdoul.gentech_fintech.DTO;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;

public class UserDTO {
    @Getter
    @Setter
    public static class SignUp{
        @NotBlank(message = "Name field can't be blank")
        @Length(min = 2, max = 30)
        @Pattern(regexp = "^[a-zA-ZÀ-ÿ]+([\\s'-][a-zA-ZÀ-ÿ]+)*$", message = "Name must only contain letters")
        private String name;

        @Email
        private String email;

        @Pattern(regexp = "^\\+?[1-9]\\d{7,14}$", message = "Phone format is not valid")
        private String phone;

        @NotBlank(message = "Password can't be blank")
        @Length(min = 10, max = 30, message = "Password must be between 10 and 30 characters")
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).+$", message = "Enter a more secure password containing symbols and letters")
        private String password;
    }
    @Getter
    @Setter
    public static class Login {
        private String email;
        private String phone;
        @NotBlank(message = "You must enter a password")
        private String password;
    }

    @Getter
    @Setter
    public static class LoginWith2fa{
        @NotBlank(message = "Token field can't be empty")
        private String token;
        @NotBlank(message = "Code field can't be empty")
        private String code;
    }

    @Getter
    @Setter
    public static class Transfer{
        @DecimalMin(value = "0.1", message = "Amount must be greater than 0.1")
        @DecimalMax(value = "10000", message = "Amount must be less or equal to 10000")
        @Digits(integer = 5, fraction = 2)
        private BigDecimal amount;
        @Email
        private String receiverEmail;
        private String receiverPhone;
        @NotBlank(message = "Enter a description")
        private String description;
    }

    @Getter
    @Setter
    public static class Refresh{
        @NotBlank(message = "Enter the token")
        private String token;
    }
}
