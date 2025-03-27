package com.example.automobile_portal.configs;

import java.nio.file.Paths;

import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class WebConfig implements WebMvcConfigurer{
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String avatarDir = Paths.get("automobile-portal/uploads/avatars/").toAbsolutePath().toUri().toString();
        registry.addResourceHandler("/uploads/avatars/**")
                .addResourceLocations(avatarDir)
                .setCachePeriod(0);

        registry.addResourceHandler("/uploads/files/**")
        .addResourceLocations("file:" + Paths.get("automobile-portal/src/main/resources/static/uploads/files/").toAbsolutePath().toString() + "/")
        .setCachePeriod(0);
    }
    
}
