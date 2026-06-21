package com.mttauto.momentum_api.friends.dto;

import com.mttauto.momentum_api.friends.entity.FriendRequest;
import com.mttauto.momentum_api.friends.entity.FriendRequestStatus;

import java.time.Instant;

public record FriendRequestResponse(
        Long id,
        FriendUserResponse sender,
        FriendUserResponse receiver,
        FriendRequestStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static FriendRequestResponse from(FriendRequest friendRequest) {
        return new FriendRequestResponse(
                friendRequest.getId(),
                FriendUserResponse.from(friendRequest.getSender()),
                FriendUserResponse.from(friendRequest.getReceiver()),
                friendRequest.getStatus(),
                friendRequest.getCreatedAt(),
                friendRequest.getUpdatedAt()
        );
    }
}
