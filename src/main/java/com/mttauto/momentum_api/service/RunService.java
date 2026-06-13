package com.mttauto.momentum_api.service;

import com.mttauto.momentum_api.dto.CreateRoutePointRequest;
import com.mttauto.momentum_api.dto.CreateRunRequest;
import com.mttauto.momentum_api.dto.RoutePointResponse;
import com.mttauto.momentum_api.dto.RunResponse;
import com.mttauto.momentum_api.entity.RoutePoint;
import com.mttauto.momentum_api.entity.Run;
import com.mttauto.momentum_api.exception.ResourceNotFoundException;
import com.mttauto.momentum_api.repository.RunRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RunService {

    private final RunRepository runRepository;

    @Transactional
    public RunResponse createRun(CreateRunRequest request) {
        validateRunRequest(request);

        Run run = new Run(
                request.startTime(),
                request.endTime(),
                request.distanceMeters(),
                request.durationSeconds(),
                request.averagePaceSecondsPerKm()
        );

        // Ensure route points are added in the correct sequence order
        request.routePoints().stream()
                .sorted(Comparator.comparing(CreateRoutePointRequest::sequenceNumber))
                .map(this::toRoutePoint)
                .forEach(run::addRoutePoint);
        
        return toRunResponse(runRepository.save(run));
    }

    @Transactional(readOnly = true)
    public RunResponse getRunById(Long id) {
        Run run = runRepository.findWithRoutePointsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Run not found with id " + id));

        return toRunResponse(run);
    }

    @Transactional(readOnly = true)
    public List<RunResponse> getAllRuns() {
        return runRepository.findAllByOrderByStartTimeDesc().stream()
                .map(this::toRunResponse)
                .toList();
    }

    @Transactional
    public void deleteRun(Long id) {
        Run run = runRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Run not found with id " + id));

        runRepository.delete(run);
    }

    private void validateRunRequest(CreateRunRequest request) {
        // Field-level validation lives on DTOs; this service keeps cross-field business rules.
        if (!request.endTime().isAfter(request.startTime())) {
            throw new IllegalArgumentException("endTime must be after startTime");
        }
    }

    private RoutePoint toRoutePoint(CreateRoutePointRequest request) {
        return new RoutePoint(
                request.latitude(),
                request.longitude(),
                request.recordedAt(),
                request.accuracyMeters(),
                request.sequenceNumber()
        );
    }

    private RunResponse toRunResponse(Run run) {
        List<RoutePointResponse> routePoints = run.getRoutePoints().stream()
                .sorted(Comparator.comparing(RoutePoint::getSequenceNumber))
                .map(this::toRoutePointResponse)
                .toList();

        return new RunResponse(
                run.getId(),
                run.getStartTime(),
                run.getEndTime(),
                run.getDistanceMeters(),
                run.getDurationSeconds(),
                run.getAveragePaceSecondsPerKm(),
                run.getCreatedAt(),
                run.getUpdatedAt(),
                routePoints
        );
    }

    private RoutePointResponse toRoutePointResponse(RoutePoint routePoint) {
        return new RoutePointResponse(
                routePoint.getId(),
                routePoint.getLatitude(),
                routePoint.getLongitude(),
                routePoint.getRecordedAt(),
                routePoint.getAccuracyMeters(),
                routePoint.getSequenceNumber()
        );
    }
}
