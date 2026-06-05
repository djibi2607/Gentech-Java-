package com.abdoul.gentech_fintech.Configuration;

import com.abdoul.gentech_fintech.Filter.JwtFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class JwtConfig {
    @Bean
    public BCryptPasswordEncoder encoder (){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public FilterRegistrationBean<JwtFilter> filterRegistrationBean (JwtFilter jwtFilter){
        FilterRegistrationBean <JwtFilter> reg = new FilterRegistrationBean<>();

        reg.setFilter(jwtFilter);
        reg.addUrlPatterns("/api/users/transfer");
        reg.addUrlPatterns("/api/agents/*");
        reg.addUrlPatterns("/api/admin/*");
        reg.setOrder(1);

        return reg;
    }
}
