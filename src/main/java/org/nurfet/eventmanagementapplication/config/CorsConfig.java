package org.nurfet.eventmanagementapplication.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Разрешаем запросы с localhost:3000
        config.addAllowedOrigin("http://localhost:3000");

        // Разрешаем все HTTP методы
        config.addAllowedMethod("*");

        // Разрешаем все заголовки
        config.addAllowedHeader("*");

        source.registerCorsConfiguration("/api/**", config);
        return new CorsFilter(source);
    }
}
