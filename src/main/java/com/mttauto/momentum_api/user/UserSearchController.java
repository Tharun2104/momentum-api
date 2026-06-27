package com.mttauto.momentum_api.user;

import com.mttauto.momentum_api.exception.ResourceNotFoundException;
import com.mttauto.momentum_api.friends.dto.FriendUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserSearchController {

    private final UserRepository userRepository;

    @GetMapping("/search")
    public ResponseEntity<FriendUserResponse> search(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Long id
    ) {
        if (id != null) {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
            return ResponseEntity.ok(FriendUserResponse.from(user));
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email or id is required");
        }
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email " + email));
        return ResponseEntity.ok(FriendUserResponse.from(user));
    }
}
