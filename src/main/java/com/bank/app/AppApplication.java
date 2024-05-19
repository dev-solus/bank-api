package com.bank.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.HandlerMapping;

import io.github.cdimascio.dotenv.Dotenv;

// @SpringBootApplication
// public class AppApplication {

// 	public static void main(String[] args) {
// 		// Dotenv dotenv = Dotenv.load();
//         // System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
//         // System.out.println("DATABASE_URL: " + dotenv.get("DATABASE_URL"));
//         // System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
// 		SpringApplication.run(AppApplication.class, args);
// 	}
// }

@SpringBootApplication(exclude = {WebMvcAutoConfiguration.class})
public class AppApplication {
    public static void main(String[] args) {
        SpringApplication.run(AppApplication.class, args);
    }

}
