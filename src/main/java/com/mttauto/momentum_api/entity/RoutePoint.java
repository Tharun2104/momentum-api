package com.mttauto.momentum_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "route_points")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoutePoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter(AccessLevel.PACKAGE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_id", nullable = false)
    private Run run;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @Column(name = "accuracy_meters")
    private Double accuracyMeters;

    @Column(name = "sequence_number", nullable = false)
    private Integer sequenceNumber;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public RoutePoint(
            Double latitude,
            Double longitude,
            Instant recordedAt,
            Double accuracyMeters,
            Integer sequenceNumber
    ) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.recordedAt = recordedAt;
        this.accuracyMeters = accuracyMeters;
        this.sequenceNumber = sequenceNumber;
    }

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
    }
}
