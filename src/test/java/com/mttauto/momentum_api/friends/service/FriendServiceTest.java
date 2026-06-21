package com.mttauto.momentum_api.friends.service;

import com.mttauto.momentum_api.auth.CurrentUserContext;
import com.mttauto.momentum_api.friends.dto.SendFriendRequest;
import com.mttauto.momentum_api.user.User;
import com.mttauto.momentum_api.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class FriendServiceTest {

    @Autowired
    private FriendService friendService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        CurrentUserContext.clear();
        userRepository.deleteAll();
    }

    @Test
    void sendRequestRejectsSelfRequest() {
        User user = userRepository.save(new User("User One", "user1@example.com", "password-hash"));
        CurrentUserContext.set(user);

        assertThatThrownBy(() -> friendService.sendRequest(new SendFriendRequest(user.getId())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot send a friend request to yourself");
    }
}
