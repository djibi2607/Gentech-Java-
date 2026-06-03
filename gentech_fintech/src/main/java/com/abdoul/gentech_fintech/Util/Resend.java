package com.abdoul.gentech_fintech.Util;

import com.abdoul.gentech_fintech.Configuration.TransType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
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

    public void sendTransactionEmail(String name, String subject, BigDecimal amount, TransType transType) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("from", "Acme <onboarding@resend.dev>");
        body.put("to", myEmail);
        body.put("subject", subject);
        body.put("html", "<p style=\"text-align: center;\"><strong>" + subject + "</strong></p>" +
                "<p>Hello " + name + ", your " + transType + " of <strong>" + amount.toPlainString() + " GNF</strong> was successful.</p>" +
                "<p>&nbsp;</p>" +
                "<p>Thank you,<br>The Gentech Team.</p>");

        webClient.post()
                .uri("/emails")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .retry(3L)
                .doOnError(error -> log.error("Email failed {}", error.getMessage()))
                .subscribe();
    }

    public void UnflagEmail (String name, String subject){
        Map<String, String> body = new LinkedHashMap<>();
        body.put("from", "Acme <onboarding@resend.dev>");
        body.put("to", myEmail);
        body.put("subject", subject);
        body.put("html", "<p style=\"text-align: center;\"><strong>" + subject + "</strong></p>" +
                "<p>Hello " + name + ", your account has been unflagged </p>" +
                "<p>&nbsp;</p>" +
                "<p>Thank you,<br>The Gentech Team.</p>");

        webClient.post()
                .uri("/emails")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .retry(3L)
                .doOnError(error -> log.error("Email failed {}", error.getMessage(), error))
                .subscribe();
    }

    public void sendKycRemainder (String name, String subject){
        Map<String, String> body = new LinkedHashMap<>();
        body.put("from", "Acme <onboarding@resend.dev>");
        body.put("to", myEmail);
        body.put("subject", subject);
        body.put("html", "<p style=\"text-align: center;\"><strong>" + subject + "</strong></p>" +
                "<p>Hello " + name + ", your account will be deactivated in 4 days without further notice if identification documents are not submitted </p>" +
                "<p>&nbsp;</p>" +
                "<p>Thank you,<br>The Gentech Team.</p>");

        webClient.post()
                .uri("/emails")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .retry(3L)
                .doOnError(error -> log.error("Email failed {}", error.getMessage(), error))
                .subscribe();
    }

    public void sendDailyReports (Long deposit, Long withdraw, Long transfer, BigDecimal amount, String subject){
        Map<String, String> body = new LinkedHashMap<>();
        body.put("from", "Acme <onboarding@resend.dev>");
        body.put("to", myEmail);
        body.put("subject", subject);
        body.put("html", "<p style=\"text-align: center;\"><strong>" + subject + "</strong></p>" +
                "<p>Hello , the total amount of deposits made today is:" + deposit +  "</p>" +
                "<p> The total amount of withdrawals made is:" + withdraw + "</p>" +
                "<p> The total amount of transfers made is:" + transfer + "</p>" +
                "<p> The total amount that moved today is:" + amount + "</p>" +
                "<p>&nbsp;</p>" +
                "<p>Thank you,<br>The Gentech Team.</p>");

        webClient.post()
                .uri("/emails")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .retry(3L)
                .doOnError(error -> log.error("Email failed {}", error.getMessage(), error))
                .subscribe();
    }
}
