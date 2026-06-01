package com.abdoul.gentech_fintech.Controller;

import com.abdoul.gentech_fintech.DTO.AdminDTO;
import com.abdoul.gentech_fintech.Models.UserModel;
import com.abdoul.gentech_fintech.Services.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService adminService;

    public AdminController (AdminService adminService){
        this.adminService = adminService;
    }

    @PostMapping("/unflag")
    public ResponseEntity<Map<String, String>> unflagUser (@RequestBody AdminDTO.Unflag data, HttpServletRequest request){
        UserModel currentUser = (UserModel) request.getAttribute("currentUser");
        return ResponseEntity.ok().body(adminService.unflagUser(data, currentUser));
    }

    @PostMapping("/promote-agent")
    public ResponseEntity<Map<String, String>> promoteAgent(@RequestBody AdminDTO.Unflag data, HttpServletRequest request){
        UserModel currentUser = (UserModel) request.getAttribute("currentUser");
        return ResponseEntity.ok().body(adminService.promoteAgent(data, currentUser));
    }

}
