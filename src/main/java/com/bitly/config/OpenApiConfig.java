package com.bitly.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for API documentation.
 */
@Configuration
public class OpenApiConfig {

    @Value("${app.base-url}")
    private String baseUrl;

    @Bean
    public OpenAPI bitlyOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bitly URL Shortener API")
                        .description("""
                                A production-grade URL shortener API built with Spring Boot.
                                
                                Features:
                                - Shorten long URLs with auto-generated or custom short codes
                                - Redirect via short codes with click tracking
                                - Optional expiration dates for links
                                - QR code generation for shortened URLs
                                - Full analytics (click count, last accessed, creation date)
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Bitly API Support")
                                .email("support@bitly.local"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url(baseUrl)
                                .description("Current Server")
                ));
    }
}
