package io.github.waynem77.bscmail4.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

/**
 * Server service application running on port 8080.
 * Provides REST API endpoints for business logic.
 */
@SpringBootApplication
@ComponentScan(basePackages = "io.github.waynem77.bscmail4.server")
public class ServerApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ServerApplication.class);
        app.setAdditionalProfiles("server");
        app.run(args);
    }
}
