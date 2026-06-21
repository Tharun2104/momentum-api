package com.mttauto.momentum_api.friends.dto;

import jakarta.validation.constraints.NotNull;

public record SendFriendRequest(
        @NotNull(message = "receiverUserId is required")
        Long receiverUserId
) {
}
