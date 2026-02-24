package com.shareint.backend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}
    
    @Bean
    public CommandLineRunner diagnosticRunner(@Value("${spring.datasource.url}") String url,
                                             @Value("${DB_PORT:n/a}") String dbPort) {
        return args -> {
            System.out.println("========================================");
            System.out.println("DIAGNOSTIC LOG");
            System.out.println("Datasource URL: " + url);
            System.out.println("DB_PORT Variable: " + dbPort);
            System.out.println("========================================");
        };
    }
    
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:3000")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }

}
