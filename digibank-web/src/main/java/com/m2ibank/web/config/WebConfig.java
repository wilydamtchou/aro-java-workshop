package com.m2ibank.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Static resources have priority over WebJars
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/static/swagger-ui/")
                .setCachePeriod(0);
        
        // WebJars fallback
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
}
