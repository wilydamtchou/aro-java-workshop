package com.m2ibank.web.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI digiBankOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DigiBank API")
                        .description("REST API documentation for the DigiBank modular monolith application")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("M2iBank DevSecOps Workshop")
                                .email("contact@m2ibank.local"))
                        .license(new License()
                                .name("Educational Use Only")
                                .url("https://example.org/license")))
                .externalDocs(new ExternalDocumentation()
                        .description("DigiBank Project Documentation")
                        .url("https://example.org/digibank"));
    }
}
