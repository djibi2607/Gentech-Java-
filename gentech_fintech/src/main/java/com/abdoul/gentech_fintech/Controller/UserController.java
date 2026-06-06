package com.abdoul.gentech_fintech.Controller;

import com.abdoul.gentech_fintech.DTO.UserDTO;
import com.abdoul.gentech_fintech.Models.UserModel;
import com.abdoul.gentech_fintech.Services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
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
        String device = request.getHeader("User-Agent");
        return ResponseEntity.ok().body(userService.createAccount(data, ip, device));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login (@Valid @RequestBody UserDTO.Login data, HttpServletRequest request){
        String ip = request.getRemoteAddr();
        String device = request.getHeader("User-Agent");
        return ResponseEntity.ok().body(userService.logIn(data, ip, device));
    }

    @PostMapping("/login-with-2-fa")
    public ResponseEntity<Map<String, String>> loginWith2fa (@Valid @RequestBody UserDTO.LoginWith2fa data, HttpServletRequest request){
        String ip = request.getRemoteAddr();
        String device = request.getHeader("User-Agent");
        return ResponseEntity.ok().body(userService.loginWith2fa(data, ip, device));
    }

    @PostMapping("/transfer")
    public ResponseEntity<Map<String,String>> transfer (@Valid @RequestBody UserDTO.Transfer data, HttpServletRequest request){
        UserModel currentUser = (UserModel) request.getAttribute("currentUser");
        String ip = request.getRemoteAddr();
        String device = request.getHeader("User-Agent");
        return ResponseEntity.ok().body(userService.transfer(data, currentUser, ip, device));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh (@Valid @RequestBody UserDTO.Refresh data, HttpServletRequest request){
        UserModel currentUser = (UserModel) request.getAttribute("currentUser");
        String ip = request.getRemoteAddr();
        String device = request.getHeader("User-Agent");
        return ResponseEntity.ok().body(userService.refreshToken(data, currentUser, ip, device));
    }

    @GetMapping("/balance")
    public ResponseEntity<Map<String, BigDecimal>> getBalance (HttpServletRequest request){
        UserModel currentUser = (UserModel) request.getAttribute("currentUser");
        return ResponseEntity.ok().body(userService.getBalance(currentUser));
    }

    @GetMapping("get-all-transactions")
    public ResponseEntity<List<UserDTO.Transactions>> getAllTransactions (HttpServletRequest request, @RequestParam(value = "page", defaultValue = "1") int page, @RequestParam(value = "size", defaultValue = "10") int size){
        UserModel currentUser = (UserModel) request.getAttribute("currentUser");
        return ResponseEntity.ok().body(userService.getAllTransactions(currentUser, page, size));
    }
}