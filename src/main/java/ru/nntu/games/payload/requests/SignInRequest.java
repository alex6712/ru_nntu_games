package ru.nntu.games.payload.requests;

import lombok.Data;

@Data
public class SignInRequest {
    private String username;

    private String password;
}
