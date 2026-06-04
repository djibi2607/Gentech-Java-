package com.abdoul.gentech_fintech.Controller;

import com.abdoul.gentech_fintech.DTO.UserDTO;
import com.abdoul.gentech_fintech.Services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController (UserService userService){
        this.userService = userService;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<Map<String, String>> createAccount (@Valid @RequestBody UserDTO.SignUp data, HttpServletRequest request){
        String ip = request.getRemoteAddr();
        return ResponseEntity.ok().body(userService.createAccount(data, ip));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login (@Valid @RequestBody UserDTO.Login data, HttpServletRequest request){
        String ip = request.getRemoteAddr();
        return ResponseEntity.ok().body(userService.logIn(data, ip));
    }

    @PostMapping("/login-with-2-fa")
    public ResponseEntity<Map<String, String>> loginWith2fa (@Valid @RequestBody UserDTO.LoginWith2fa data, HttpServletRequest request){
        String ip = request.getRemoteAddr();
        return ResponseEntity.ok().body(userService.loginWith2fa(data, ip));
    }
}