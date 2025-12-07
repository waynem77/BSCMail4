package io.github.waynem77.bscmail4;

import io.github.waynem77.bscmail4.server.ServerApplication;
import io.github.waynem77.bscmail4.client.ClientApplication;
import org.springframework.boot.SpringApplication;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Launcher that starts both server and client services in a single JAR.
 * This allows running both services from one executable.
 * 
 * Usage:
 *   java -jar app.jar
 * 
 * This will start:
 *   - Server service on http://localhost:8080
 *   - Client service on http://localhost:8081
 */
public class Launcher {

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // Start server service in a separate thread
        executor.submit(() -> {
            try {
                SpringApplication serverApp = new SpringApplication(ServerApplication.class);
                serverApp.setAdditionalProfiles("server");
                serverApp.run(args);
            } catch (Exception e) {
                System.err.println("Failed to start server service: " + e.getMessage());
                e.printStackTrace();
            }
        });

        // Give server a moment to start
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Start client service in a separate thread
        executor.submit(() -> {
            try {
                SpringApplication clientApp = new SpringApplication(ClientApplication.class);
                clientApp.setAdditionalProfiles("client");
                clientApp.run(args);
            } catch (Exception e) {
                System.err.println("Failed to start client service: " + e.getMessage());
                e.printStackTrace();
            }
        });

        // Keep the main thread alive and handle shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down services...");
            executor.shutdown();
            try {
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }));

        // Keep main thread alive
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

