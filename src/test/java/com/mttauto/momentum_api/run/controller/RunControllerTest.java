package com.mttauto.momentum_api.run.controller;

import com.mttauto.momentum_api.run.repository.RunRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RunControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RunRepository runRepository;

    @Autowired
    private UserRepository userRepository;

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        runRepository.deleteAll();
        userRepository.deleteAll();
        token = register("Runner", "runner@example.com", "password123");
    }

    @Test
    void createRunReturnsFieldErrorForInvalidRoutePointLatitude() throws Exception {
        String requestBody = """
                {
                  "startTime": "2026-06-13T10:00:00Z",
                  "endTime": "2026-06-13T10:20:00Z",
                  "distanceMeters": 3000,
                  "durationSeconds": 1200,
                  "averagePaceSecondsPerKm": 400,
                  "routePoints": [
                    {
                      "latitude": 100,
                      "longitude": -74.006,
                      "recordedAt": "2026-06-13T10:00:10Z",
                      "accuracyMeters": 5,
                      "sequenceNumber": 1
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/runs")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("routePoints[0].latitude: latitude must be between -90 and 90"));
    }

    @Test
    void createRunReturnsStepCounts() throws Exception {
        String requestBody = """
                {
                  "startTime": "2026-06-13T10:00:00Z",
                  "endTime": "2026-06-13T10:20:00Z",
                  "distanceMeters": 3000,
                  "durationSeconds": 1200,
                  "averagePaceSecondsPerKm": 400,
                  "appStepCount": 2550,
                  "healthKitStartStepCount": 47000,
                  "healthKitEndStepCount": 49410,
                  "healthKitUpdateLagSeconds": 90,
                  "routePoints": [
                    {
                      "latitude": 35.2271,
                      "longitude": -80.8431,
                      "recordedAt": "2026-06-13T10:00:10Z",
                      "accuracyMeters": 5,
                      "sequenceNumber": 1
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/runs")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.appStepCount").value(2550))
                .andExpect(jsonPath("$.healthKitStartStepCount").value(47000))
                .andExpect(jsonPath("$.healthKitEndStepCount").value(49410))
                .andExpect(jsonPath("$.healthKitStepCount").value(2410))
                .andExpect(jsonPath("$.healthKitUpdateLagSeconds").value(90));
    }

    @Test
    void createRunReturnsFieldErrorForNegativeAppStepCount() throws Exception {
        String requestBody = """
                {
                  "startTime": "2026-06-13T10:00:00Z",
                  "endTime": "2026-06-13T10:20:00Z",
                  "distanceMeters": 3000,
                  "durationSeconds": 1200,
                  "averagePaceSecondsPerKm": 400,
                  "appStepCount": -1,
                  "routePoints": [
                    {
                      "latitude": 35.2271,
                      "longitude": -80.8431,
                      "recordedAt": "2026-06-13T10:00:10Z",
                      "accuracyMeters": 5,
                      "sequenceNumber": 1
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/runs")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("appStepCount: appStepCount must be greater than or equal to 0"));
    }

    @Test
    void deleteRunRemovesSavedRun() throws Exception {
        Long runId = createRun();

        mockMvc.perform(delete("/api/runs/{id}", runId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/runs/{id}", runId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Run not found with id " + runId));
    }

    @Test
    void deleteRunReturnsNotFoundWhenRunDoesNotExist() throws Exception {
        mockMvc.perform(delete("/api/runs/{id}", 99L)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Run not found with id 99"));
    }

    @Test
    void protectedRunApisRequireJwtToken() throws Exception {
        mockMvc.perform(get("/api/runs"))
                .andExpect(status().isUnauthorized());
    }

    private Long createRun() throws Exception {
        String requestBody = """
                {
                  "startTime": "2026-06-13T10:00:00Z",
                  "endTime": "2026-06-13T10:20:00Z",
                  "distanceMeters": 3000,
                  "durationSeconds": 1200,
                  "averagePaceSecondsPerKm": 400,
                  "routePoints": [
                    {
                      "latitude": 35.2271,
                      "longitude": -80.8431,
                      "recordedAt": "2026-06-13T10:00:10Z",
                      "accuracyMeters": 5,
                      "sequenceNumber": 1
                    },
                    {
                      "latitude": 35.2275,
                      "longitude": -80.8428,
                      "recordedAt": "2026-06-13T10:00:20Z",
                      "accuracyMeters": 5,
                      "sequenceNumber": 2
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/runs")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        return runRepository.findAll().get(0).getId();
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
