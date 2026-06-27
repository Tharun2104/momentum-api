package com.mttauto.momentum_api.friends.controller;

import com.mttauto.momentum_api.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FriendControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private String tokenUser1;
    private String tokenUser2;
    private String tokenUser3;
    private Long user1Id;
    private Long user2Id;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();
        tokenUser1 = register("User One", "user1@example.com", "password123");
        tokenUser2 = register("User Two", "user2@example.com", "password123");
        tokenUser3 = register("User Three", "user3@example.com", "password123");
        user1Id = userRepository.findByEmail("user1@example.com").orElseThrow().getId();
        user2Id = userRepository.findByEmail("user2@example.com").orElseThrow().getId();
    }

    @Test
    void searchUserByEmailReturnsSafeFields() throws Exception {
        mockMvc.perform(get("/api/users/search")
                        .header("Authorization", bearer(tokenUser1))
                        .param("email", "user2@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user2Id))
                .andExpect(jsonPath("$.name").value("User Two"))
                .andExpect(jsonPath("$.email").value("user2@example.com"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void searchUserByIdReturnsSafeFields() throws Exception {
        mockMvc.perform(get("/api/users/search")
                        .header("Authorization", bearer(tokenUser1))
                        .param("id", user2Id.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user2Id))
                .andExpect(jsonPath("$.name").value("User Two"))
                .andExpect(jsonPath("$.email").value("user2@example.com"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void searchUserByEmailReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/users/search")
                        .header("Authorization", bearer(tokenUser1))
                        .param("email", "missing@example.com"))
                .andExpect(status().isNotFound());
    }

    @Test
    void friendRequestCanBeAcceptedAndListsBothDirections() throws Exception {
        Long requestId = sendFriendRequest(user2Id, tokenUser1);

        mockMvc.perform(get("/api/friends/requests/incoming")
                        .header("Authorization", bearer(tokenUser2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(requestId))
                .andExpect(jsonPath("$[0].sender.email").value("user1@example.com"))
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        mockMvc.perform(get("/api/friends/requests/outgoing")
                        .header("Authorization", bearer(tokenUser1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].receiver.email").value("user2@example.com"));

        mockMvc.perform(post("/api/friends/requests/{requestId}/accept", requestId)
                        .header("Authorization", bearer(tokenUser2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        mockMvc.perform(get("/api/friends")
                        .header("Authorization", bearer(tokenUser1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("user2@example.com"));

        mockMvc.perform(get("/api/friends")
                        .header("Authorization", bearer(tokenUser2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("user1@example.com"));
    }

    @Test
    void friendRequestCanBeRejected() throws Exception {
        Long requestId = sendFriendRequest(user2Id, tokenUser1);

        mockMvc.perform(post("/api/friends/requests/{requestId}/reject", requestId)
                        .header("Authorization", bearer(tokenUser2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        mockMvc.perform(get("/api/friends")
                        .header("Authorization", bearer(tokenUser1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void userCanDeleteExistingFriend() throws Exception {
        Long requestId = sendFriendRequest(user2Id, tokenUser1);

        mockMvc.perform(post("/api/friends/requests/{requestId}/accept", requestId)
                        .header("Authorization", bearer(tokenUser2)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/friends/{friendUserId}", user2Id)
                        .header("Authorization", bearer(tokenUser1)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/friends")
                        .header("Authorization", bearer(tokenUser1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/api/friends")
                        .header("Authorization", bearer(tokenUser2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        sendFriendRequest(user1Id, tokenUser2);
    }

    @Test
    void preventsSelfAndDuplicateRequests() throws Exception {
        mockMvc.perform(post("/api/friends/request")
                        .header("Authorization", bearer(tokenUser1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "receiverUserId": %d
                                }
                                """.formatted(user1Id)))
                .andExpect(status().isBadRequest());

        sendFriendRequest(user2Id, tokenUser1);

        mockMvc.perform(post("/api/friends/request")
                        .header("Authorization", bearer(tokenUser1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "receiverUserId": %d
                                }
                                """.formatted(user2Id)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void preventsRequestWhenAlreadyFriends() throws Exception {
        Long requestId = sendFriendRequest(user2Id, tokenUser1);
        mockMvc.perform(post("/api/friends/requests/{requestId}/accept", requestId)
                        .header("Authorization", bearer(tokenUser2)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/friends/request")
                        .header("Authorization", bearer(tokenUser2))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "receiverUserId": %d
                                }
                                """.formatted(user1Id)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("You are already friends with this user"));
    }

    @Test
    void unauthorizedUserCannotAcceptRequest() throws Exception {
        Long requestId = sendFriendRequest(user2Id, tokenUser1);

        mockMvc.perform(post("/api/friends/requests/{requestId}/accept", requestId)
                        .header("Authorization", bearer(tokenUser3)))
                .andExpect(status().isNotFound());
    }

    @Test
    void friendApisRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/friends"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void corsPreflightRequestsAreAllowedBeforeAuthentication() throws Exception {
        mockMvc.perform(options("/api/friends")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().isOk());
    }

    private Long sendFriendRequest(Long receiverUserId, String token) throws Exception {
        String body = mockMvc.perform(post("/api/friends/request")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "receiverUserId": %d
                                }
                                """.formatted(receiverUserId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return Long.parseLong(body.substring(body.indexOf(":") + 1, body.indexOf(",")));
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String register(String name, String email, String password) throws Exception {
        String body = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(name, email, password)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return body.substring(body.indexOf(":\"") + 2, body.indexOf("\","));
    }
}
