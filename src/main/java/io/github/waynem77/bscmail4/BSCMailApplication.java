package io.github.waynem77.bscmail4;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BSCMailApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(BSCMailApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext applicationContext)
    {
        return args ->
        {
            System.out.println("Hello, world.");
        };
    }
}
