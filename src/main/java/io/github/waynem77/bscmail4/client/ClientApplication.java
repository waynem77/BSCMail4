package io.github.waynem77.bscmail4.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

/**
 * Client service application running on port 8081.
 * Provides Thymeleaf-based web interface that communicates with server via REST.
 */
@SpringBootApplication
@ComponentScan(basePackages = "io.github.waynem77.bscmail4.client")
public class ClientApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ClientApplication.class);
        app.setAdditionalProfiles("client");
        app.run(args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}


