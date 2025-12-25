package io.github.waynem77.bscmail4;

import io.github.waynem77.bscmail4.client.ClientApplication;
import io.github.waynem77.bscmail4.server.ServerApplication;
import org.springframework.boot.SpringApplication;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Launcher that starts server and/or client services based on active profiles.
 * This allows running both services from one executable, or just one service.
 * <p>
 * Usage:
 * java -jar app.jar --spring.profiles.active=server,client,dev
 * java -jar app.jar --spring.profiles.active=server,dev
 * java -jar app.jar --spring.profiles.active=client
 * <p>
 * This will start:
 * - Server service on http://localhost:8080 (if 'server' profile is active)
 * - Client service on http://localhost:8081 (if 'client' profile is active)
 */
public class Launcher
{

    public static void main(String[] args)
    {
        // Parse active profiles from args
        List<String> activeProfiles = parseActiveProfiles(args);
        boolean startServer = activeProfiles.contains("server");
        boolean startClient = activeProfiles.contains("client");

        if (!startServer && !startClient)
        {
            System.err.println("Error: At least one of 'server' or 'client' profile must be active.");
            System.err.println("Usage: --spring.profiles.active=server,client,dev");
            System.exit(1);
        }

        int servicesToStart = (startServer ? 1 : 0) + (startClient ? 1 : 0);
        ExecutorService executor = Executors.newFixedThreadPool(servicesToStart);

        // Start server service if 'server' profile is active
        if (startServer)
        {
            executor.submit(() -> {
                try
                {
                    System.out.println("Starting server service on port 8080...");
                    SpringApplication serverApp = new SpringApplication(ServerApplication.class);
                    // Filter profiles to only include server-related ones
                    String[] serverProfiles = filterProfiles(activeProfiles, "server");
                    serverApp.setAdditionalProfiles(serverProfiles);
                    // Remove profile args and pass filtered ones
                    String[] serverArgs = filterArgs(args, serverProfiles);
                    serverApp.run(serverArgs);
                }
                catch (Exception e)
                {
                    System.err.println("Failed to start server service: " + e.getMessage());
                    e.printStackTrace();
                }
            });

            // Give server a moment to start before starting client
            if (startClient)
            {
                try
                {
                    Thread.sleep(2000);
                }
                catch (InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                }
            }
        }

        // Start client service if 'client' profile is active
        if (startClient)
        {
            executor.submit(() -> {
                try
                {
                    System.out.println("Starting client service on port 8081...");
                    SpringApplication clientApp = new SpringApplication(ClientApplication.class);
                    // Filter profiles to only include client-related ones
                    String[] clientProfiles = filterProfiles(activeProfiles, "client");
                    clientApp.setAdditionalProfiles(clientProfiles);
                    // Remove profile args and pass filtered ones
                    String[] clientArgs = filterArgs(args, clientProfiles);
                    clientApp.run(clientArgs);
                }
                catch (Exception e)
                {
                    System.err.println("Failed to start client service: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }

        // Keep the main thread alive and handle shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down services...");
            executor.shutdown();
            try
            {
                if (!executor.awaitTermination(10, TimeUnit.SECONDS))
                {
                    executor.shutdownNow();
                }
            }
            catch (InterruptedException e)
            {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }));

        // Keep main thread alive
        try
        {
            Thread.currentThread().join();
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Parses active profiles from command line arguments.
     *
     * @param args command line arguments
     * @return list of active profile names
     */
    private static List<String> parseActiveProfiles(String[] args)
    {
        for (String arg : args)
        {
            if (arg.startsWith("--spring.profiles.active="))
            {
                String profiles = arg.substring("--spring.profiles.active=".length());
                return Arrays.asList(profiles.split(","));
            }
        }
        // Default: start both if no profiles specified
        return Arrays.asList("server", "client");
    }

    /**
     * Filters profiles to only include the service profile and related profiles (dev, prod, test).
     *
     * @param activeProfiles all active profiles
     * @param serviceProfile the service profile to keep (server or client)
     * @return filtered array of profiles
     */
    private static String[] filterProfiles(List<String> activeProfiles, String serviceProfile)
    {
        return activeProfiles.stream()
                .filter(profile -> profile.equals(serviceProfile) ||
                        profile.equals("dev") ||
                        profile.equals("prod") ||
                        profile.equals("test"))
                .toArray(String[]::new);
    }

    /**
     * Filters command line arguments to replace profile args with service-specific profiles.
     *
     * @param args     original command line arguments
     * @param profiles service-specific profiles to use
     * @return filtered arguments array
     */
    private static String[] filterArgs(String[] args, String[] profiles)
    {
        return Arrays.stream(args)
                .filter(arg -> !arg.startsWith("--spring.profiles.active="))
                .toArray(String[]::new);
    }
}

