package com.mttauto.momentum_api.friends.service;

import com.mttauto.momentum_api.auth.CurrentUserContext;
import com.mttauto.momentum_api.exception.ResourceNotFoundException;
import com.mttauto.momentum_api.friends.dto.FriendRequestResponse;
import com.mttauto.momentum_api.friends.dto.FriendUserResponse;
import com.mttauto.momentum_api.friends.dto.SendFriendRequest;
import com.mttauto.momentum_api.friends.entity.FriendRequest;
import com.mttauto.momentum_api.friends.entity.FriendRequestStatus;
import com.mttauto.momentum_api.friends.repository.FriendRequestRepository;
import com.mttauto.momentum_api.user.User;
import com.mttauto.momentum_api.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendRequestRepository friendRequestRepository;
    private final UserRepository userRepository;

    @Transactional
    public FriendRequestResponse sendRequest(SendFriendRequest request) {
        User sender = CurrentUserContext.get();
        User receiver = userRepository.findById(request.receiverUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + request.receiverUserId()));

        if (sender.getId().equals(receiver.getId())) {
            throw new IllegalArgumentException("Cannot send a friend request to yourself");
        }
        if (friendRequestRepository.existsBetweenUsersWithStatus(sender.getId(), receiver.getId(), FriendRequestStatus.PENDING)) {
            throw new IllegalArgumentException("A pending friend request already exists");
        }
        if (friendRequestRepository.existsBetweenUsersWithStatus(sender.getId(), receiver.getId(), FriendRequestStatus.ACCEPTED)) {
            throw new IllegalArgumentException("You are already friends with this user");
        }

        return FriendRequestResponse.from(friendRequestRepository.save(new FriendRequest(sender, receiver)));
    }

    @Transactional(readOnly = true)
    public List<FriendRequestResponse> getIncomingRequests() {
        return friendRequestRepository.findByReceiver_IdAndStatusOrderByCreatedAtDesc(
                        CurrentUserContext.get().getId(),
                        FriendRequestStatus.PENDING
                ).stream()
                .map(FriendRequestResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FriendRequestResponse> getOutgoingRequests() {
        return friendRequestRepository.findBySender_IdAndStatusOrderByCreatedAtDesc(
                        CurrentUserContext.get().getId(),
                        FriendRequestStatus.PENDING
                ).stream()
                .map(FriendRequestResponse::from)
                .toList();
    }

    @Transactional
    public FriendRequestResponse acceptRequest(Long requestId) {
        FriendRequest friendRequest = findPendingRequestForReceiver(requestId);
        friendRequest.accept();
        return FriendRequestResponse.from(friendRequest);
    }

    @Transactional
    public FriendRequestResponse rejectRequest(Long requestId) {
        FriendRequest friendRequest = findPendingRequestForReceiver(requestId);
        friendRequest.reject();
        return FriendRequestResponse.from(friendRequest);
    }

    @Transactional(readOnly = true)
    public List<FriendUserResponse> getFriends() {
        Long currentUserId = CurrentUserContext.get().getId();
        return friendRequestRepository.findAcceptedFriendships(currentUserId).stream()
                .map(friendRequest -> friend(friendRequest, currentUserId))
                .sorted(Comparator.comparing(FriendUserResponse::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional
    public void deleteFriend(Long friendUserId) {
        Long currentUserId = CurrentUserContext.get().getId();
        FriendRequest friendship = friendRequestRepository.findAcceptedFriendship(currentUserId, friendUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Friend not found with id " + friendUserId));
        friendRequestRepository.delete(friendship);
    }

    private FriendRequest findPendingRequestForReceiver(Long requestId) {
        FriendRequest friendRequest = friendRequestRepository.findByIdAndReceiver_Id(
                        requestId,
                        CurrentUserContext.get().getId()
                )
                .orElseThrow(() -> new ResourceNotFoundException("Friend request not found with id " + requestId));
        validatePending(friendRequest);
        return friendRequest;
    }

    private void validatePending(FriendRequest friendRequest) {
        if (friendRequest.getStatus() != FriendRequestStatus.PENDING) {
            throw new IllegalArgumentException("Friend request is not pending");
        }
    }

    private FriendUserResponse friend(FriendRequest friendRequest, Long currentUserId) {
        User friend = friendRequest.getSender().getId().equals(currentUserId)
                ? friendRequest.getReceiver()
                : friendRequest.getSender();
        return FriendUserResponse.from(friend);
    }
}
