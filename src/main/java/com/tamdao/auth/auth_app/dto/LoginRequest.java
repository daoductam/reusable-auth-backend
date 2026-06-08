package com.tamdao.auth.auth_app.dto;

public record LoginRequest(
        String email,
        String password
) {
}
