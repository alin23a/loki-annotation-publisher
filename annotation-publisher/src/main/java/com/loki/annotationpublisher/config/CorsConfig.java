package com.loki.annotationpublisher.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(LokiConfig.class)
public class CorsConfig {

    private static final Logger logger = LogManager.getLogger(CorsConfig.class);

    @Bean
    RestTemplate lokiRestTemplate(final LokiConfig lokiConfig) {
        logger.info("Creating Loki RestTemplate with baseUrl={}", lokiConfig.getUrl());

        return new RestTemplate();
    }

    @Bean
    WebMvcConfigurer corsConfigurer() {
        logger.info("Configuring CORS for frontend origin http://localhost:5173");

        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(final CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("http://localhost:5173")
                        .allowedMethods("GET", "POST", "OPTIONS")
                        .allowedHeaders("*");
            }
        };
    }
}
