package com.mttauto.momentum_api.friends.controller;

import com.mttauto.momentum_api.friends.dto.FriendRequestResponse;
import com.mttauto.momentum_api.friends.dto.FriendUserResponse;
import com.mttauto.momentum_api.friends.dto.SendFriendRequest;
import com.mttauto.momentum_api.friends.service.FriendService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @PostMapping("/request")
    public ResponseEntity<FriendRequestResponse> sendRequest(@Valid @RequestBody SendFriendRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(friendService.sendRequest(request));
    }

    @GetMapping("/requests/incoming")
    public ResponseEntity<List<FriendRequestResponse>> incomingRequests() {
        return ResponseEntity.ok(friendService.getIncomingRequests());
    }

    @GetMapping("/requests/outgoing")
    public ResponseEntity<List<FriendRequestResponse>> outgoingRequests() {
        return ResponseEntity.ok(friendService.getOutgoingRequests());
    }

    @PostMapping("/requests/{requestId}/accept")
    public ResponseEntity<FriendRequestResponse> acceptRequest(@PathVariable Long requestId) {
        return ResponseEntity.ok(friendService.acceptRequest(requestId));
    }

    @PostMapping("/requests/{requestId}/reject")
    public ResponseEntity<FriendRequestResponse> rejectRequest(@PathVariable Long requestId) {
        return ResponseEntity.ok(friendService.rejectRequest(requestId));
    }

    @GetMapping
    public ResponseEntity<List<FriendUserResponse>> friends() {
        return ResponseEntity.ok(friendService.getFriends());
    }

    @DeleteMapping("/{friendUserId}")
    public ResponseEntity<Void> deleteFriend(@PathVariable Long friendUserId) {
        friendService.deleteFriend(friendUserId);
        return ResponseEntity.noContent().build();
    }
}
