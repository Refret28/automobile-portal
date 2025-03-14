package com.example.automobile_portal.configs;

import java.nio.file.Paths;

import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class WebConfig implements WebMvcConfigurer{
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadDir = Paths.get("automobile-portal/uploads/avatars/").toAbsolutePath().toUri().toString();

        registry.addResourceHandler("/uploads/avatars/**")
                .addResourceLocations(uploadDir) 
                .setCachePeriod(0);
    }
}
