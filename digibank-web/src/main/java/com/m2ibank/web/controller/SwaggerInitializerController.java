package com.m2ibank.web.controller;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SwaggerInitializerController {

    @GetMapping("/swagger-ui/swagger-initializer.js")
    @ResponseBody
    public String getSwaggerInitializer() {
        return """
            window.onload = function() {
              window.ui = SwaggerUIBundle({
                url: "/v3/api-docs",
                dom_id: '#swagger-ui',
                deepLinking: true,
                presets: [
                  SwaggerUIBundle.presets.apis,
                  SwaggerUIStandalonePreset
                ],
                plugins: [
                  SwaggerUIBundle.plugins.DownloadUrl
                ],
                layout: "StandaloneLayout",
                validatorUrl: ""
              });
            };
            """;
    }
}
