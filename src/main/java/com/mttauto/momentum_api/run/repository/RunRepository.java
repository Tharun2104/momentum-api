package com.mttauto.momentum_api.run.repository;

import com.mttauto.momentum_api.run.entity.Run;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RunRepository extends JpaRepository<Run, Long> {

    @EntityGraph(attributePaths = "routePoints")
    Optional<Run> findWithRoutePointsById(Long id);

    @EntityGraph(attributePaths = "routePoints")
    List<Run> findAllByOrderByStartTimeDesc();
}
