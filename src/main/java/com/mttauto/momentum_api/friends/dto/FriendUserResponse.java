package com.mttauto.momentum_api.friends.dto;

import com.mttauto.momentum_api.user.User;

public record FriendUserResponse(
        Long id,
        String name,
        String email
) {
    public static FriendUserResponse from(User user) {
        return new FriendUserResponse(user.getId(), user.getName(), user.getEmail());
    }
}
