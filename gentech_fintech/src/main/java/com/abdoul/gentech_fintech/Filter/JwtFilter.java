package com.abdoul.gentech_fintech.Filter;


import com.abdoul.gentech_fintech.Models.UserModel;
import com.abdoul.gentech_fintech.Repositories.UserRepository;
import com.abdoul.gentech_fintech.Util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtil jwt;
    private final UserRepository userRepository;

    public JwtFilter(JwtUtil jwt, UserRepository userRepository){
        this.jwt = jwt;
        this.userRepository = userRepository;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String token = request.getHeader("Authorization");

        if (token == null){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("User authentication failed");
            return;
        }

        if (!token.startsWith("Bearer ")){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid credentials");
            return;
        }

        String cleanToken = token.split(" ")[1];

        if (!jwt.isTokenValid(cleanToken)){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid credentials");
            return;
        }

        String id = jwt.extractIdFromToken(cleanToken);

        UserModel currentUser = userRepository.findById(Long.parseLong(id))
                .orElse(null);

        if (currentUser == null){
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("User not found");
            return;
        }

        request.setAttribute("currentUser", currentUser);
        filterChain.doFilter(request, response);

    }
}
