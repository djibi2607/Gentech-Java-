package com.abdoul.gentech_fintech;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GentechFintechApplication {

	public static void main(String[] args) {
		SpringApplication.run(GentechFintechApplication.class, args);
	}

}
