package com.abdoul.gentech_fintech.Controller;

import com.abdoul.gentech_fintech.DTO.UserDTO;
import com.abdoul.gentech_fintech.Services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController (UserService userService){
        this.userService = userService;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<Map<String, String>> createAccount (@Valid @RequestBody UserDTO.SignUp data){
        return ResponseEntity.ok().body(userService.createAccount(data));
    }
}