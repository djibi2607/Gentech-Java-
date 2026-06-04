package com.abdoul.gentech_fintech.Configuration;

import com.abdoul.gentech_fintech.Util.FraudDetection;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FraudConfig {
    @Bean
    public FilterRegistrationBean<FraudDetection> registrationFraudBean (FraudDetection fraudDetection){
        FilterRegistrationBean<FraudDetection> reg = new FilterRegistrationBean<>();
        reg.addUrlPatterns("/api/*");
        reg.setFilter(fraudDetection);
        reg.setOrder(3);
        return reg;
    }
}
