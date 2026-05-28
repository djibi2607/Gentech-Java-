package com.abdoul.gentech_fintech.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

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
}
