package com.example.auth.dto;
import java.util.UUID;

public class MessageResponse {
    private String message;

    private UUID userId;

    public MessageResponse(String message, UUID userId) {
        this.message = message;
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}
