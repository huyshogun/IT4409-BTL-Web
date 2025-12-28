package com.example.gateway.config;

import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("${app.jwtCookieName}")
    private String jwtCookieName;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            
            // --- 1. Kích hoạt CORS trong Security ---
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            .authorizeExchange(exchanges -> exchanges
                // --- 2. Cho phép OPTIONS đi qua  ---
                .pathMatchers(HttpMethod.OPTIONS).permitAll()
                
                // 1. Public API
                .pathMatchers("/api/v1/auth/**", "/eureka/**", "/webjars/**", "/v3/api-docs/**", "/actuator/**").permitAll()

                // 2. API Admin
                .pathMatchers("/api/v1/account/admin/**").hasRole("ADMIN")

                // 3. API User: Kiểm tra quyền sở hữu
                .pathMatchers("/api/v1/account/{userId}").access(checkUserId())

                // 4. Chặn nội bộ
                .pathMatchers("/api/v1/account/internalApi/**").denyAll()

                // 5. Các API khác
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .bearerTokenConverter(cookieTokenConverter())
                .jwt(jwt -> jwt
                    .jwtDecoder(jwtDecoder())
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
                .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
            );

        return http.build();
    }

    // --- 3. CẤU HÌNH CORS JAVA BEAN  ---
    @Bean
    public CorsWebFilter corsWebFilter() {
        // Filter này có độ ưu tiên cao, chạy trước Security
        return new CorsWebFilter(corsConfigurationSource());
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        
        // Cho phép tất cả các nguồn 
        corsConfig.setAllowedOriginPatterns(Collections.singletonList("*"));
        
        // Cho phép tất cả các Method
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"));
        
        // Cho phép tất cả Header
        corsConfig.setAllowedHeaders(Collections.singletonList("*"));
        
        // Quan trọng: Cho phép gửi Cookie/Token
        corsConfig.setAllowCredentials(true);
        
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return source;
    }

    private ReactiveAuthorizationManager<AuthorizationContext> checkUserId() {
        return (authentication, context) -> 
            authentication.map(auth -> {
                boolean isAdmin = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
                if (isAdmin) return new AuthorizationDecision(true);

                ServerHttpRequest request = context.getExchange().getRequest();
                String path = request.getURI().getPath();
                String userIdFromUrl = path.substring(path.lastIndexOf('/') + 1);
                String userIdFromToken = auth.getName(); 

                return new AuthorizationDecision(userIdFromToken.equals(userIdFromUrl));
            });
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
        return NimbusReactiveJwtDecoder.withSecretKey(secretKey).macAlgorithm(MacAlgorithm.HS256).build();
    }

    @Bean
    public Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter);
    }

    private ServerAuthenticationConverter cookieTokenConverter() {
        return exchange -> {
            String token = null;
            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
            if (token == null) {
                HttpCookie cookie = exchange.getRequest().getCookies().getFirst(jwtCookieName);
                if (cookie != null) token = cookie.getValue();
            }
            if (token != null && !token.trim().isEmpty()) {
                return Mono.just(new BearerTokenAuthenticationToken(token));
            }
            return Mono.empty();
        };
    }
}