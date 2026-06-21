package com.mttauto.momentum_api.moneytrack.repository;

import com.mttauto.momentum_api.moneytrack.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

    List<PaymentMethod> findByUser_IdOrderByCreatedAtDesc(Long userId);

    Optional<PaymentMethod> findByIdAndUser_Id(Long id, Long userId);
}
