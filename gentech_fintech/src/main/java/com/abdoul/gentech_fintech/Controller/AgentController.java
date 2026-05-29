package com.abdoul.gentech_fintech.Controller;

import com.abdoul.gentech_fintech.DTO.AgentDTO;
import com.abdoul.gentech_fintech.Models.UserModel;
import com.abdoul.gentech_fintech.Services.AgentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        return ResponseEntity.ok().body(agentService.getUserCredentials(data, currentUser));
    }

}
