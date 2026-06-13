package com.mttauto.momentum_api.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "runs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Run {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @Column(name = "distance_meters", nullable = false)
    private Double distanceMeters;

    @Column(name = "duration_seconds", nullable = false)
    private Long durationSeconds;

    @Column(name = "average_pace_seconds_per_km", nullable = false)
    private Double averagePaceSecondsPerKm;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = "run", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequenceNumber ASC")
    private List<RoutePoint> routePoints = new ArrayList<>();

    public Run(
            Instant startTime,
            Instant endTime,
            Double distanceMeters,
            Long durationSeconds,
            Double averagePaceSecondsPerKm
    ) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.distanceMeters = distanceMeters;
        this.durationSeconds = durationSeconds;
        this.averagePaceSecondsPerKm = averagePaceSecondsPerKm;
    }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public void addRoutePoint(RoutePoint routePoint) {
        // Keep the bidirectional JPA relationship in sync from one place.
        routePoints.add(routePoint);
        routePoint.setRun(this);
    }

    public void removeRoutePoint(RoutePoint routePoint) {
        routePoints.remove(routePoint);
        routePoint.setRun(null);
    }

    public List<RoutePoint> getRoutePoints() {
        // Callers should use add/remove helpers instead of mutating the collection directly.
        return Collections.unmodifiableList(routePoints);
    }
}
