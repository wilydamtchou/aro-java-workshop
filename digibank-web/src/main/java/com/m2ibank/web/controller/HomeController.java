package com.m2ibank.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("applicationName", "DigiBank");
        model.addAttribute("applicationDescription", "Digital Core Banking System for M2iBank");
        model.addAttribute("swaggerUrl", "/swagger-ui.html");
        return "index";
    }
}
