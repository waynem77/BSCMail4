package io.github.waynem77.bscmail4.client.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Home controller for root URL redirects.
 */
@Slf4j
@Controller
public class HomeController
{
    /**
     * Test endpoint to verify controller is registered.
     *
     * @return test message
     */
    @GetMapping("/test")
    @ResponseBody
    public String test()
    {
        log.info("Test endpoint accessed");
        return "Test endpoint is working! Controllers are registered.";
    }

    /**
     * Redirects root URL to the person list.
     *
     * @return redirect to person list
     */
    @GetMapping("/")
    public String home()
    {
        log.info("Home controller accessed, redirecting to /person");
        return "redirect:/person";
    }
}

