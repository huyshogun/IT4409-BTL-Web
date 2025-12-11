package com.example.gateway.config;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

@Component
public class CustomAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED); // Trả về mã 401
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // Tạo nội dung lỗi JSON
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("code", 401);
        errorDetails.put("status", "Unauthorized");
        errorDetails.put("message", "Token không hợp lệ, hết hạn hoặc bị thiếu!");
        
        // Ghi log lỗi ra console (Tùy chọn)
        // System.out.println("Lỗi xác thực: " + ex.getMessage());

        try {
            byte[] bytes = objectMapper.writeValueAsString(errorDetails).getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
    }
}
