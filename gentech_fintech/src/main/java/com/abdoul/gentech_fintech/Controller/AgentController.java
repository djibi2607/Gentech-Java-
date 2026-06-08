package com.abdoul.gentech_fintech.Controller;

import com.abdoul.gentech_fintech.DTO.AgentDTO;
import com.abdoul.gentech_fintech.DTO.UserDTO;
import com.abdoul.gentech_fintech.Models.KycModel;
import com.abdoul.gentech_fintech.Models.UserModel;
import com.abdoul.gentech_fintech.Services.AgentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.aspectj.weaver.loadtime.Agent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agents")
public class AgentController {
    private final AgentService agentService;
    public AgentController(AgentService agentService){
        this.agentService = agentService;
    }

    @PostMapping("/get-credentials")
    public ResponseEntity<Map<String, String>> getUserCredentials (HttpServletRequest request, @RequestBody AgentDTO.UserCredentials data){
        UserModel currentUser = (UserModel) request.getAttribute("currentUser");
        String ip = request.getRemoteAddr();
        String device = request.getHeader("User-Agent");
        return ResponseEntity.ok().body(agentService.getUserCredentials(data, currentUser, ip, device));
    }

    @PostMapping("/deposit")
    public ResponseEntity<Map<String, String>> deposit(HttpServletRequest request, @Valid @RequestBody AgentDTO.DepositWith data){
        UserModel currentUser = (UserModel) request.getAttribute("currentUser");
        String ip = request.getRemoteAddr();
        String device = request.getHeader("User-Agent");
        return ResponseEntity.ok().body(agentService.deposit(data, currentUser, ip, device));
    }

    @PostMapping("/deposit-with-2-fa")
    public ResponseEntity<Map<String, String>> depositWith2fa (HttpServletRequest request, @Valid @RequestBody AgentDTO.DepositWith2fa data){
        UserModel currentUser = (UserModel) request.getAttribute("currentUser");
        String ip = request.getRemoteAddr();
        String device = request.getHeader("User-Agent");
        return ResponseEntity.ok().body(agentService.depositWith2fa(data, currentUser, ip, device));
    }

    @PostMapping("withdraw")
    public ResponseEntity<Map<String ,String>> withdraw (HttpServletRequest request, @Valid @RequestBody AgentDTO.DepositWith data){
        UserModel currentUser = (UserModel) request.getAttribute("currentUser");
        String ip = request.getRemoteAddr();
        String device = request.getHeader("User-Agent");
        return  ResponseEntity.ok().body(agentService.withdraw(data, currentUser, ip, device));
    }

    @PostMapping("withdraw-with-2fa")
    public ResponseEntity<Map<String, String>> withdrawWith2fa (HttpServletRequest request, @Valid @RequestBody AgentDTO.DepositWith2fa data){
        UserModel currentUser = (UserModel) request.getAttribute("currentUser");
        String ip = request.getRemoteAddr();
        String device = request.getHeader("User-Agent");
        return ResponseEntity.ok().body(agentService.withdrawWith2fa(data, currentUser, ip, device));
    }

    @PatchMapping("/flag")
    public ResponseEntity<Map<String, String>> flagUser (HttpServletRequest request, @Valid @RequestBody AgentDTO.Flag data){
        UserModel currentUser = (UserModel) request.getAttribute("currentUser");
        String ip = request.getRemoteAddr();
        String device = request.getHeader("User-Agent");
        return ResponseEntity.ok().body(agentService.flagUser(data, currentUser, ip, device));
    }

    @GetMapping("/kyc")
    public ResponseEntity<List<UserDTO.KycDetails>> getUnsolvedKyc (HttpServletRequest request, @RequestParam(value = "page", defaultValue = "1") int page, @RequestParam(value = "size", defaultValue = "10") int size){
        UserModel currentUser = (UserModel) request.getAttribute("currentUser");
        return ResponseEntity.ok().body(agentService.getUnSolvedKyc(currentUser, page, size));
    }

    @PostMapping("/url")
    public ResponseEntity<Map<String, String>> getGeneratedUrl (HttpServletRequest request, @RequestBody AgentDTO.Url data){
        UserModel currentUser = (UserModel) request.getAttribute("currentUser");
        String ip = request.getRemoteAddr();
        String device = request.getHeader("User-Agent");
        return ResponseEntity.ok().body(agentService.generateUrl(currentUser, ip, device, data));
    }

    @PatchMapping("/verify-kyc")
    public ResponseEntity<Map<String, String>> verifyKyc (@RequestBody AgentDTO.Kyc data, HttpServletRequest request){
        UserModel currentUser = (UserModel) request.getAttribute("currentUser");
        String ip = request.getRemoteAddr();
        String device = request.getHeader("User-Agent");
        return ResponseEntity.ok().body(agentService.handleKyc(data, currentUser, ip, device));
    }
}
