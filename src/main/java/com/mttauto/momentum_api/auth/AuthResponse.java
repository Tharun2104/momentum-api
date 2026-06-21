package com.mttauto.momentum_api.auth;

public record AuthResponse(
        String accessToken,
        UserResponse user
) {
}
