package com.abdoul.gentech_fintech.Filter;

import com.abdoul.gentech_fintech.Models.UserModel;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        UserModel currentUser = (UserModel) request.getAttribute("currentUser");

        if (currentUser == null){
            String ip = request.getRemoteAddr();

            Bucket bucket = buckets.computeIfAbsent(ip, newBucket -> createBucket(5L));

            if (!bucket.tryConsume(1)){
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Too many requests");
                return;
            }

            filterChain.doFilter(request, response);
            return;

        }

        Bucket bucket = buckets.computeIfAbsent(String.valueOf(currentUser.getId()), user -> createBucket(10L));

        if (!bucket.tryConsume(1)){
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Too many requests");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private Bucket createBucket (Long capacity){
        Refill refill = Refill.greedy(capacity, Duration.ofHours(1));
        Bandwidth bandwidth = Bandwidth.classic(capacity, refill);
        return Bucket.builder().addLimit(bandwidth).build();
    }
}
