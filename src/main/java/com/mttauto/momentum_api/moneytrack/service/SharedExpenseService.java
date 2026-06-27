package com.mttauto.momentum_api.moneytrack.service;

import com.mttauto.momentum_api.auth.CurrentUserContext;
import com.mttauto.momentum_api.exception.ResourceNotFoundException;
import com.mttauto.momentum_api.friends.dto.FriendUserResponse;
import com.mttauto.momentum_api.moneytrack.dto.SharedExpenseParticipantResponse;
import com.mttauto.momentum_api.moneytrack.dto.SharedExpenseResponse;
import com.mttauto.momentum_api.moneytrack.entity.SharedExpense;
import com.mttauto.momentum_api.moneytrack.entity.SharedExpenseParticipant;
import com.mttauto.momentum_api.moneytrack.repository.ExpenseRepository;
import com.mttauto.momentum_api.moneytrack.repository.SharedExpenseRepository;
import com.mttauto.momentum_api.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SharedExpenseService {

    private final SharedExpenseRepository sharedExpenseRepository;
    private final ExpenseRepository expenseRepository;

    @Transactional(readOnly = true)
    public List<SharedExpenseResponse> getSharedExpenses() {
        Long currentUserId = CurrentUserContext.get().getId();
        return sharedExpenseRepository.findVisibleToUser(currentUserId).stream()
                .map(sharedExpense -> toResponse(sharedExpense, currentUserId))
                .toList();
    }

    @Transactional(readOnly = true)
    public SharedExpenseResponse getSharedExpense(Long id) {
        Long currentUserId = CurrentUserContext.get().getId();
        return sharedExpenseRepository.findByIdVisibleToUser(id, currentUserId)
                .map(sharedExpense -> toResponse(sharedExpense, currentUserId))
                .orElseThrow(() -> new ResourceNotFoundException("Shared expense not found with id " + id));
    }

    @Transactional
    public void deleteSharedExpense(Long id) {
        Long currentUserId = CurrentUserContext.get().getId();
        SharedExpense sharedExpense = sharedExpenseRepository.findByIdVisibleToUser(id, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Shared expense not found with id " + id));
        Long originalExpenseId = sharedExpense.getOriginalExpense() == null
                ? null
                : sharedExpense.getOriginalExpense().getId();

        sharedExpenseRepository.delete(sharedExpense);
        if (originalExpenseId != null) {
            expenseRepository.deleteById(originalExpenseId);
        }
    }

    SharedExpenseResponse toResponse(SharedExpense sharedExpense, Long currentUserId) {
        SharedExpenseParticipant currentParticipant = currentParticipant(sharedExpense, currentUserId);
        User otherUser = otherUser(sharedExpense, currentUserId);
        String otherUserName = otherUserName(sharedExpense, currentUserId, otherUser);
        return new SharedExpenseResponse(
                sharedExpense.getId(),
                sharedExpense.getTitle(),
                sharedExpense.getTotalAmount(),
                sharedExpense.getCategory(),
                sharedExpense.getExpenseDate(),
                sharedExpense.getPaidBy().getId(),
                sharedExpense.getPaidBy().getName(),
                sharedExpense.getFriend().getId(),
                sharedExpense.getFriend().getName(),
                currentParticipant.getShareAmount(),
                currentParticipant.getPaidAmount(),
                currentParticipant.getNetAmount(),
                otherUserName,
                displayText(otherUserName, currentParticipant.getNetAmount()),
                FriendUserResponse.from(sharedExpense.getCreatedBy()),
                FriendUserResponse.from(sharedExpense.getPaidBy()),
                FriendUserResponse.from(sharedExpense.getFriend()),
                sharedExpense.getOriginalExpense() == null ? null : sharedExpense.getOriginalExpense().getId(),
                sharedExpense.getSplitType(),
                sharedExpense.getStatus(),
                sharedExpense.getParticipants().stream().map(this::toParticipantResponse).toList(),
                sharedExpense.getCreatedAt(),
                sharedExpense.getUpdatedAt()
        );
    }

    private SharedExpenseParticipantResponse toParticipantResponse(SharedExpenseParticipant participant) {
        return new SharedExpenseParticipantResponse(
                participant.getId(),
                FriendUserResponse.from(participant.getUser()),
                participant.getShareAmount(),
                participant.getPaidAmount(),
                participant.getNetAmount(),
                participant.getCreatedAt(),
                participant.getUpdatedAt()
        );
    }

    private SharedExpenseParticipant currentParticipant(SharedExpense sharedExpense, Long currentUserId) {
        return sharedExpense.getParticipants().stream()
                .filter(participant -> participant.getUser().getId().equals(currentUserId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Shared expense participant not found"));
    }

    private User otherUser(SharedExpense sharedExpense, Long currentUserId) {
        if (sharedExpense.getPaidBy().getId().equals(currentUserId)) {
            return sharedExpense.getFriend();
        }

        return sharedExpense.getPaidBy();
    }

    private String otherUserName(SharedExpense sharedExpense, Long currentUserId, User fallbackUser) {
        if (!sharedExpense.getPaidBy().getId().equals(currentUserId)) {
            return fallbackUser.getName();
        }

        List<String> names = sharedExpense.getParticipants().stream()
                .map(SharedExpenseParticipant::getUser)
                .filter(user -> !user.getId().equals(currentUserId))
                .map(User::getName)
                .toList();
        if (names.size() == 1) {
            return names.get(0);
        }
        return names.size() + " friends";
    }

    static String displayText(String otherUserName, BigDecimal netAmount) {
        int comparison = netAmount.compareTo(BigDecimal.ZERO);
        if (otherUserName.endsWith(" friends") && comparison > 0) {
            return "You are owed " + formatMoney(netAmount);
        }
        if (comparison > 0) {
            return otherUserName + " owes you " + formatMoney(netAmount);
        }
        if (comparison < 0) {
            return "You owe " + otherUserName + " " + formatMoney(netAmount.abs());
        }
        return "Settled up with " + otherUserName;
    }

    static String formatMoney(BigDecimal amount) {
        BigDecimal normalized = amount.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros();
        return "$" + normalized.toPlainString();
    }
}
