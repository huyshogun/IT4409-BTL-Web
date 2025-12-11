package com.example.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity // Bắt buộc dùng Annotation này vì Gateway chạy trên WebFlux (Netty)
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable) // Tắt CSRF vì dùng API Stateless
            .authorizeExchange(exchanges -> exchanges
                // 1. Cho phép truy cập tự do vào Auth Service (Đăng nhập/Đăng ký)
                .pathMatchers("/api/v1/auth/**").permitAll()
                // 2. Cho phép truy cập Eureka dashboard (nếu có) hoặc Swagger
                .pathMatchers("/eureka/**", "/webjars/**").permitAll()
                // 3. Tất cả các request còn lại BẮT BUỘC phải có Token hợp lệ
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> {}) // Kích hoạt kiểm tra JWT theo cấu hình trong YML
                // (Tùy chọn) Cấu hình xử lý lỗi khi Token sai
                .authenticationEntryPoint(new CustomAuthenticationEntryPoint()) 
            );

        return http.build();
    }
}