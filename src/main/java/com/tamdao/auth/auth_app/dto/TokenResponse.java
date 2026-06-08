package com.tamdao.auth.auth_app.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        String tokenType,
        UserDTO userDTO

) {
    public static TokenResponse of(String accessToken, String refreshToken, long expiresIn, UserDTO userDTO) {
        return new TokenResponse(accessToken, refreshToken, expiresIn, "Bearer", userDTO);
    }
}
