package com.mttauto.momentum_api.moneytrack.repository;

import com.mttauto.momentum_api.moneytrack.entity.SharedExpenseParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SharedExpenseParticipantRepository extends JpaRepository<SharedExpenseParticipant, Long> {
}
