package com.mttauto.momentum_api.service;

import com.mttauto.momentum_api.dto.CreateRoutePointRequest;
import com.mttauto.momentum_api.dto.CreateRunRequest;
import com.mttauto.momentum_api.dto.RunResponse;
import com.mttauto.momentum_api.exception.ResourceNotFoundException;
import com.mttauto.momentum_api.repository.RoutePointRepository;
import com.mttauto.momentum_api.repository.RunRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class RunServiceTest {

    @Autowired
    private RunRepository runRepository;

    @Autowired
    private RoutePointRepository routePointRepository;

    @Autowired
    private RunService runService;

    @BeforeEach
    void setUp() {
        runRepository.deleteAll();
    }

    @Test
    void createRunSavesRunWithRoutePoints() {
        CreateRunRequest request = createValidRequest();

        RunResponse response = runService.createRun(request);

        assertThat(response.id()).isNotNull();
        assertThat(response.distanceMeters()).isEqualTo(1000.0);
        assertThat(response.durationSeconds()).isEqualTo(360L);
        assertThat(response.routePoints()).hasSize(2);
        assertThat(response.routePoints().get(0).sequenceNumber()).isEqualTo(1);
        assertThat(response.routePoints().get(1).sequenceNumber()).isEqualTo(2);
    }

    @Test
    void createRunSavesStepCountsAndCalculatesHealthKitStepCount() {
        CreateRunRequest request = new CreateRunRequest(
                Instant.parse("2026-06-12T10:00:00Z"),
                Instant.parse("2026-06-12T10:06:00Z"),
                1000.0,
                360L,
                360.0,
                870L,
                12_400L,
                13_225L,
                45L,
                List.of(validRoutePoint(1), validRoutePoint(2))
        );

        RunResponse response = runService.createRun(request);

        assertThat(response.appStepCount()).isEqualTo(870L);
        assertThat(response.healthKitStartStepCount()).isEqualTo(12_400L);
        assertThat(response.healthKitEndStepCount()).isEqualTo(13_225L);
        assertThat(response.healthKitStepCount()).isEqualTo(825L);
        assertThat(response.healthKitUpdateLagSeconds()).isEqualTo(45L);
    }

    @Test
    void createRunRejectsHealthKitEndStepCountBeforeStartStepCount() {
        CreateRunRequest request = new CreateRunRequest(
                Instant.parse("2026-06-12T10:00:00Z"),
                Instant.parse("2026-06-12T10:06:00Z"),
                1000.0,
                360L,
                360.0,
                870L,
                12_400L,
                12_399L,
                45L,
                List.of(validRoutePoint(1))
        );

        assertThatThrownBy(() -> runService.createRun(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("healthKitEndStepCount must be greater than or equal to healthKitStartStepCount");
    }

    @Test
    void createRunRejectsEndTimeBeforeStartTime() {
        CreateRunRequest request = new CreateRunRequest(
                Instant.parse("2026-06-12T10:10:00Z"),
                Instant.parse("2026-06-12T10:00:00Z"),
                1000.0,
                360L,
                360.0,
                null,
                null,
                null,
                null,
                List.of(validRoutePoint(1))
        );

        assertThatThrownBy(() -> runService.createRun(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("endTime must be after startTime");
    }

    @Test
    void getRunByIdThrowsWhenRunDoesNotExist() {
        assertThatThrownBy(() -> runService.getRunById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Run not found with id 99");
    }

    @Test
    void deleteRunRemovesRunAndRoutePoints() {
        RunResponse response = runService.createRun(createValidRequest());

        runService.deleteRun(response.id());

        assertThat(runRepository.findById(response.id())).isEmpty();
        assertThat(routePointRepository.count()).isZero();
        assertThatThrownBy(() -> runService.getRunById(response.id()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Run not found with id " + response.id());
    }

    @Test
    void deleteRunThrowsWhenRunDoesNotExist() {
        assertThatThrownBy(() -> runService.deleteRun(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Run not found with id 99");
    }

    private CreateRunRequest createValidRequest() {
        return new CreateRunRequest(
                Instant.parse("2026-06-12T10:00:00Z"),
                Instant.parse("2026-06-12T10:06:00Z"),
                1000.0,
                360L,
                360.0,
                null,
                null,
                null,
                null,
                List.of(validRoutePoint(1), validRoutePoint(2))
        );
    }

    private CreateRoutePointRequest validRoutePoint(int sequenceNumber) {
        return new CreateRoutePointRequest(
                40.7128,
                -74.0060,
                Instant.parse("2026-06-12T10:01:00Z").plusSeconds(sequenceNumber),
                5.0,
                sequenceNumber
        );
    }
}
