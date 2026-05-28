package com.abdoul.gentech_fintech.Util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Slf4j
public class Resend {
    @Value("${my.email}")
    private String myEmail;

    private WebClient webClient;

    public Resend (WebClient webClient){
        this.webClient = webClient;
    }

    public void sendWelcomeEmail (String name, String subject){
        Map<String, String> body = new LinkedHashMap<>();

        body.put("from", "Acme <onboarding@resend.dev>");
        body.put("to", myEmail);
        body.put("subject", subject);
        body.put("html", "<p style=\"text-align: center;\"><strong>Account Creation</strong></p>" +
                "<p>Welcome to Gentech, " + name + ". Your account has successfully been created. Please proceed to login and upload the required documents, otherwise your account will be deactivated in 14 days.</p>" +
                "<p>&nbsp;</p>" +
                "<p>Thank you,<br>The Gentech Team.</p>");

        webClient.post()
                .uri("/emails")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .retry(3L)
                .doOnError(error-> log.error("Email failed {}", error.getMessage()))
                .subscribe();
    }

    public void sendCodeEmail(String name, String subject, String code){
        Map<String, String> body = new LinkedHashMap<>();
        body.put("from", "Acme <onboarding@resend.dev>");
        body.put("to", myEmail);
        body.put("subject", subject);
        body.put("html", "<p style=\"text-align: center;\"><strong>Account Login</strong></p>" +
                "<p>Welcome back, " + name + ". Your verification code is " + code + "</p>" +
                "<p>&nbsp;</p>" +
                "<p>Thank you,<br>The Gentech Team.</p>");

        webClient.post()
                .uri("/emails")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .retry(3L)
                .doOnError(error-> log.error("Email failed {}", error.getMessage()))
                .subscribe();
    }
}
