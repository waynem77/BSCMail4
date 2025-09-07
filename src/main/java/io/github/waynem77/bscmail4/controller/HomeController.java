package io.github.waynem77.bscmail4.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for the home page.
 */
@Controller
public class HomeController
{
    /**
     * Displays the home page.
     *
     * @return Thymeleaf template name
     */
    @GetMapping("/")
    public String home()
    {
        return "index";
    }
}