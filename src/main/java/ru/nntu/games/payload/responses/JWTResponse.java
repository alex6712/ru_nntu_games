package ru.nntu.games.payload.responses;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class JWTResponse {
    private String token;

    private String type = "Bearer";

    private UUID id;

    private String username;

    private String email;

    private List<String> roles;

    public JWTResponse(String token, UUID id, String username, String email, List<String> roles) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }
}
