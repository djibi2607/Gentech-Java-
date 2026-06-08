package com.abdoul.gentech_fintech.Configuration;

import com.abdoul.gentech_fintech.Filter.RateLimitingFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimitingConfig {
    @Bean
    public FilterRegistrationBean<RateLimitingFilter> registrationBean (RateLimitingFilter rateLimitingFilter){
        FilterRegistrationBean<RateLimitingFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(rateLimitingFilter);
        reg.addUrlPatterns("/api/users/*");
        reg.addUrlPatterns("/api/agents/deposit");
        reg.addUrlPatterns("/api/agents/deposit-with-2-fa");
        reg.addUrlPatterns("/api/agents/withdraw");
        reg.addUrlPatterns("/api/agents/withdraw-with-2-fa");
        reg.addUrlPatterns("/api/agents/flag");
        reg.addUrlPatterns("/api/agents/url");
        reg.addUrlPatterns("/api/agents/kyc");
        reg.addUrlPatterns("/api/agents/verify-kyc");
        reg.setOrder(2);

        return reg;
    }
}
