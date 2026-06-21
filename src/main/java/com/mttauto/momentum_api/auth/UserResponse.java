package com.mttauto.momentum_api.auth;

import com.mttauto.momentum_api.user.User;

public record UserResponse(
        Long id,
        String name,
        String email
) {
    static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail());
    }
}
