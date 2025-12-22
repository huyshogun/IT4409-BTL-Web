package com.example.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
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
import org.springframework.http.HttpCookie;

import reactor.core.publisher.Mono;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

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
            .authorizeExchange(exchanges -> exchanges
                // 1. Public API
                .pathMatchers("/api/v1/auth/**", "/eureka/**", "/webjars/**", "/v3/api-docs/**").permitAll()

                // 2. API Admin (Giữ nguyên)
                .pathMatchers("/api/v1/account/admin/**").hasRole("ADMIN")

                // 3. API User: Kiểm tra ID trong URL có khớp ID trong Token không?
                // Dùng .access() để chạy logic tùy chỉnh (checkUserId)
                .pathMatchers("/api/v1/account/{userId}").access(checkUserId()) 

                // 4. Chặn truy cập trực tiếp qua accountNumber
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

    /**
     * --- LOGIC KIỂM TRA QUYỀN SỞ HỮU ---
     * So sánh userId trên URL với userId (Subject) trong Token
     */
    private ReactiveAuthorizationManager<AuthorizationContext> checkUserId() {
        return (authentication, context) -> 
            authentication.map(auth -> {
                // 1. Nếu là ADMIN thì cho qua luôn, không cần check ID
                boolean isAdmin = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
                if (isAdmin) {
                    return new AuthorizationDecision(true);
                }

                // 2. Lấy userId từ URL (Path Variable)
                // URL dạng: /api/v1/account/08745881-22d6...
                ServerHttpRequest request = context.getExchange().getRequest();
                String path = request.getURI().getPath();
                
                // Cắt chuỗi để lấy phần cuối cùng (userId)
                String userIdFromUrl = path.substring(path.lastIndexOf('/') + 1);
                System.out.println("Chuỗi lấy được từ URL là: " + userIdFromUrl);

                // 3. Lấy userId từ Token
                // auth.getName() trả về "sub" (subject) trong JWT.
                String userIdFromToken = auth.getName(); 
                System.out.println("Chuỗi thật là: " + userIdFromToken);

                // 4. So sánh
                boolean isMatch = userIdFromToken.equals(userIdFromUrl);
                
                // Debug log (nếu cần xem tại sao sai)
                // System.out.println("URL ID: " + userIdFromUrl + " | Token ID: " + userIdFromToken);

                return new AuthorizationDecision(isMatch);
            });
    }

    // --- CÁC BEAN CŨ GIỮ NGUYÊN (Decoder, Converter...) ---
    
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