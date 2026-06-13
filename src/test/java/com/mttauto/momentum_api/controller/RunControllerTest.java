package com.mttauto.momentum_api.controller;

import com.mttauto.momentum_api.repository.RunRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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

    @BeforeEach
    void setUp() {
        runRepository.deleteAll();
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("routePoints[0].latitude: latitude must be between -90 and 90"));
    }

    @Test
    void deleteRunRemovesSavedRun() throws Exception {
        Long runId = createRun();

        mockMvc.perform(delete("/api/runs/{id}", runId))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/runs/{id}", runId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Run not found with id " + runId));
    }

    @Test
    void deleteRunReturnsNotFoundWhenRunDoesNotExist() throws Exception {
        mockMvc.perform(delete("/api/runs/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Run not found with id 99"));
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        return runRepository.findAll().get(0).getId();
    }
}
