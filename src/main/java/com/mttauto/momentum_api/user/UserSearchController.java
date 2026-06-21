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
    public ResponseEntity<FriendUserResponse> search(@RequestParam String email) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email " + email));
        return ResponseEntity.ok(FriendUserResponse.from(user));
    }
}
