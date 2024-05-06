package com.lelexuetang.system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class GlobalCorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        /**
         * 配置CORS策略，使其允许所有的请求头、方法和来源，并且设置允许凭据。
         * 这段代码通常用于需要对跨域请求进行宽松限制的场景，例如开发阶段或API测试。
         * 但在生产环境中，出于安全考虑，应更严格地限制允许的头、方法和来源。
         */
        corsConfiguration.addAllowedHeader("*"); // 允许所有请求头
        corsConfiguration.addAllowedMethod("*"); // 允许所有HTTP请求方法
        corsConfiguration.addAllowedOrigin("*"); // 允许所有来源访问
        corsConfiguration.setAllowCredentials(true); // 允许请求携带凭据，如cookies
        UrlBasedCorsConfigurationSource corsConfigurationSource = new UrlBasedCorsConfigurationSource();
        corsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsFilter(corsConfigurationSource);
    }
}
