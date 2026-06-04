package com.abdoul.gentech_fintech.Filter;

import com.abdoul.gentech_fintech.Models.AuditLogs;
import com.abdoul.gentech_fintech.Models.UserModel;
import com.abdoul.gentech_fintech.Repositories.LogRepository;
import com.abdoul.gentech_fintech.Repositories.UserRepository;
import com.abdoul.gentech_fintech.Util.IpUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
public class FraudDetection extends OncePerRequestFilter{
    private final LogRepository logRepository;
    private final IpUtil ipUtil;
    private final UserRepository userRepository;

    public FraudDetection (LogRepository logRepository, IpUtil ipUtil, UserRepository userRepository){
        this.logRepository = logRepository;
        this.ipUtil = ipUtil;
        this.userRepository = userRepository;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        UserModel currentUser = (UserModel) request.getAttribute("currentUser");

        String myIp = request.getRemoteAddr();

        Map<String, String> infos = ipUtil.getIpDetails(myIp);

        if (currentUser == null){
            filterChain.doFilter(request, response);
            return;
        }

        AuditLogs LastLog = logRepository.findTopByUserOrderByInitiatedAtDesc(currentUser);

        if (LastLog == null || LastLog.getCountry() == null){
            filterChain.doFilter(request, response);
            return;
        }

        if (!LastLog.getCountry().equals(infos.get("Country"))){
            currentUser.setFlagged(true);
            userRepository.save(currentUser);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Suspicious location detected. Your account has been flagged.");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
