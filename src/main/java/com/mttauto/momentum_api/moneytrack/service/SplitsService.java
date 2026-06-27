package com.mttauto.momentum_api.moneytrack.service;

import com.mttauto.momentum_api.auth.CurrentUserContext;
import com.mttauto.momentum_api.moneytrack.dto.FriendBalanceResponse;
import com.mttauto.momentum_api.moneytrack.dto.SharedExpenseResponse;
import com.mttauto.momentum_api.moneytrack.dto.SplitsSummaryResponse;
import com.mttauto.momentum_api.moneytrack.entity.SharedExpense;
import com.mttauto.momentum_api.moneytrack.entity.SharedExpenseParticipant;
import com.mttauto.momentum_api.moneytrack.repository.SharedExpenseRepository;
import com.mttauto.momentum_api.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SplitsService {

    private final SharedExpenseRepository sharedExpenseRepository;
    private final SharedExpenseService sharedExpenseService;

    @Transactional(readOnly = true)
    public SplitsSummaryResponse getSummary() {
        Long currentUserId = CurrentUserContext.get().getId();
        List<BigDecimal> netAmounts = sharedExpenseRepository.findVisibleToUser(currentUserId).stream()
                .map(sharedExpense -> currentParticipant(sharedExpense, currentUserId).getNetAmount())
                .toList();
        BigDecimal netBalance = netAmounts.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalOwedToYou = netAmounts.stream()
                .filter(amount -> amount.compareTo(BigDecimal.ZERO) > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalYouOwe = netAmounts.stream()
                .filter(amount -> amount.compareTo(BigDecimal.ZERO) < 0)
                .map(BigDecimal::abs)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new SplitsSummaryResponse(scale(netBalance), scale(totalOwedToYou), scale(totalYouOwe));
    }

    @Transactional(readOnly = true)
    public List<FriendBalanceResponse> getFriendBalances() {
        Long currentUserId = CurrentUserContext.get().getId();
        Map<Long, FriendBalanceAccumulator> balances = new LinkedHashMap<>();
        for (SharedExpense sharedExpense : sharedExpenseRepository.findVisibleToUser(currentUserId)) {
            SharedExpenseParticipant participant = currentParticipant(sharedExpense, currentUserId);
            for (SharedExpenseParticipant otherParticipant : otherParticipants(sharedExpense, currentUserId)) {
                User friend = otherParticipant.getUser();
                balances.computeIfAbsent(friend.getId(), ignored -> new FriendBalanceAccumulator(friend))
                        .add(balanceAgainstParticipant(sharedExpense, participant, otherParticipant));
            }
        }

        return balances.values().stream()
                .map(FriendBalanceAccumulator::toResponse)
                .sorted(Comparator.comparing(FriendBalanceResponse::netBalance).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SharedExpenseResponse> getRecent() {
        return sharedExpenseService.getSharedExpenses();
    }

    private SharedExpenseParticipant currentParticipant(SharedExpense sharedExpense, Long currentUserId) {
        return sharedExpense.getParticipants().stream()
                .filter(participant -> participant.getUser().getId().equals(currentUserId))
                .findFirst()
                .orElseThrow();
    }

    private User otherUser(SharedExpense sharedExpense, Long currentUserId) {
        if (sharedExpense.getPaidBy().getId().equals(currentUserId)) {
            return sharedExpense.getFriend();
        }

        return sharedExpense.getPaidBy();
    }

    private List<SharedExpenseParticipant> otherParticipants(SharedExpense sharedExpense, Long currentUserId) {
        if (!sharedExpense.getPaidBy().getId().equals(currentUserId)) {
            return List.of(currentParticipant(sharedExpense, sharedExpense.getPaidBy().getId()));
        }

        return sharedExpense.getParticipants().stream()
                .filter(participant -> !participant.getUser().getId().equals(currentUserId))
                .toList();
    }

    private BigDecimal balanceAgainstParticipant(
            SharedExpense sharedExpense,
            SharedExpenseParticipant currentParticipant,
            SharedExpenseParticipant otherParticipant
    ) {
        if (sharedExpense.getPaidBy().getId().equals(currentParticipant.getUser().getId())) {
            return otherParticipant.getShareAmount();
        }

        return currentParticipant.getNetAmount();
    }

    private BigDecimal scale(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private class FriendBalanceAccumulator {
        private final User friend;
        private BigDecimal netBalance = BigDecimal.ZERO;

        private FriendBalanceAccumulator(User friend) {
            this.friend = friend;
        }

        private void add(BigDecimal netAmount) {
            netBalance = netBalance.add(netAmount);
        }

        private FriendBalanceResponse toResponse() {
            BigDecimal scaledNetBalance = scale(netBalance);
            return new FriendBalanceResponse(
                    friend.getId(),
                    friend.getName(),
                    scaledNetBalance,
                    SharedExpenseService.displayText(friend.getName(), scaledNetBalance)
            );
        }
    }
}
