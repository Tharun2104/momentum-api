package com.mttauto.momentum_api.moneytrack.service;

import com.mttauto.momentum_api.auth.CurrentUserContext;
import com.mttauto.momentum_api.friends.entity.FriendRequestStatus;
import com.mttauto.momentum_api.friends.repository.FriendRequestRepository;
import com.mttauto.momentum_api.moneytrack.dto.CreateExpenseRequest;
import com.mttauto.momentum_api.moneytrack.dto.CreateExpenseSplitRequest;
import com.mttauto.momentum_api.moneytrack.dto.ExpenseResponse;
import com.mttauto.momentum_api.moneytrack.dto.ExpenseSplitSummaryResponse;
import com.mttauto.momentum_api.moneytrack.dto.UpdateExpenseRequest;
import com.mttauto.momentum_api.moneytrack.entity.Expense;
import com.mttauto.momentum_api.moneytrack.entity.PaymentMethod;
import com.mttauto.momentum_api.exception.ResourceNotFoundException;
import com.mttauto.momentum_api.moneytrack.entity.SharedExpense;
import com.mttauto.momentum_api.moneytrack.entity.SharedExpenseParticipant;
import com.mttauto.momentum_api.moneytrack.entity.SplitType;
import com.mttauto.momentum_api.moneytrack.repository.ExpenseRepository;
import com.mttauto.momentum_api.moneytrack.repository.SharedExpenseRepository;
import com.mttauto.momentum_api.user.User;
import com.mttauto.momentum_api.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final SharedExpenseRepository sharedExpenseRepository;
    private final PaymentMethodService paymentMethodService;
    private final FriendRequestRepository friendRequestRepository;
    private final UserRepository userRepository;

    @Transactional
    public ExpenseResponse createExpense(CreateExpenseRequest request) {
        User user = CurrentUserContext.get();
        PaymentMethod paymentMethod = resolvePaymentMethod(request.paymentMethodId(), user.getId());
        if (isSplitEnabled(request.split())) {
            return createSplitExpense(request, user, paymentMethod);
        }

        Expense expense = new Expense(
                user,
                request.amount(),
                request.category(),
                cleanOptionalText(request.merchantName()),
                paymentMethod,
                request.expenseDate(),
                cleanOptionalText(request.notes())
        );

        return toResponse(expenseRepository.save(expense));
    }

    private ExpenseResponse createSplitExpense(CreateExpenseRequest request, User user, PaymentMethod paymentMethod) {
        CreateExpenseSplitRequest split = request.split();
        List<Long> friendUserIds = splitFriendUserIds(split);
        validateSplitRequestBasics(split, user.getId(), friendUserIds);
        List<User> friends = friendUserIds.stream()
                .map(friendUserId -> userRepository.findById(friendUserId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + friendUserId)))
                .toList();
        friends.forEach(friend -> validateAcceptedFriendship(user.getId(), friend.getId()));

        BigDecimal userShare = request.amount().divide(BigDecimal.valueOf(friends.size() + 1L), 2, RoundingMode.HALF_UP);
        Expense expense = expenseRepository.save(new Expense(
                user,
                userShare,
                request.category(),
                cleanOptionalText(request.merchantName()),
                paymentMethod,
                request.expenseDate(),
                cleanOptionalText(request.notes())
        ));

        SharedExpense sharedExpense = new SharedExpense(
                user,
                user,
                friends.get(0),
                expense,
                splitTitle(request),
                request.amount(),
                request.category(),
                request.expenseDate(),
                SplitType.EQUAL
        );
        sharedExpense.addParticipant(new SharedExpenseParticipant(
                user,
                userShare,
                request.amount(),
                request.amount().subtract(userShare)
        ));
        friends.forEach(friend -> sharedExpense.addParticipant(new SharedExpenseParticipant(
                friend,
                userShare,
                BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                userShare.negate()
        )));
        sharedExpenseRepository.save(sharedExpense);

        return toResponse(expense);
    }

    private List<Long> splitFriendUserIds(CreateExpenseSplitRequest split) {
        Set<Long> friendUserIds = new LinkedHashSet<>();
        if (split.friendUserId() != null) {
            friendUserIds.add(split.friendUserId());
        }
        if (split.friendUserIds() != null) {
            friendUserIds.addAll(split.friendUserIds());
        }
        return new ArrayList<>(friendUserIds);
    }

    private void validateSplitRequestBasics(CreateExpenseSplitRequest split, Long userId, List<Long> friendUserIds) {
        if (friendUserIds.isEmpty()) {
            throw new IllegalArgumentException("At least one friend is required when split is enabled");
        }
        if (friendUserIds.contains(userId)) {
            throw new IllegalArgumentException("Cannot split an expense with yourself");
        }
        if (split.splitType() != SplitType.EQUAL) {
            throw new IllegalArgumentException("Only EQUAL split is supported");
        }
    }

    private void validateAcceptedFriendship(Long userId, Long friendUserId) {
        if (!friendRequestRepository.existsBetweenUsersWithStatus(userId, friendUserId, FriendRequestStatus.ACCEPTED)) {
            throw new IllegalArgumentException("Friend relationship must be accepted");
        }
    }

    private boolean isSplitEnabled(CreateExpenseSplitRequest split) {
        return split != null && split.enabled();
    }

    private String splitTitle(CreateExpenseRequest request) {
        String merchantName = cleanOptionalText(request.merchantName());
        return merchantName == null ? request.category().name() : merchantName;
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getExpenses() {
        return expenseRepository.findByUser_IdOrderByExpenseDateDescCreatedAtDesc(CurrentUserContext.get().getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getExpenses(YearMonth month, Long paymentMethodId) {
        Long userId = CurrentUserContext.get().getId();
        if (month == null && paymentMethodId == null) {
            return getExpenses();
        }

        YearMonth selectedMonth = month == null ? YearMonth.now() : month;
        LocalDate startDate = selectedMonth.atDay(1);
        LocalDate endDate = selectedMonth.atEndOfMonth();

        if (paymentMethodId != null) {
            paymentMethodService.findPaymentMethod(paymentMethodId, userId);
            return expenseRepository.findByUser_IdAndExpenseDateBetweenAndPaymentMethodIdOrderByExpenseDateDescCreatedAtDesc(
                            userId,
                            startDate,
                            endDate,
                            paymentMethodId
                    ).stream()
                    .map(this::toResponse)
                    .toList();
        }

        return expenseRepository.findByUser_IdAndExpenseDateBetweenOrderByExpenseDateDescCreatedAtDesc(
                        userId,
                        startDate,
                        endDate
                ).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getExpense(Long id) {
        return toResponse(findExpense(id, CurrentUserContext.get().getId()));
    }

    @Transactional
    public ExpenseResponse updateExpense(Long id, UpdateExpenseRequest request) {
        Long userId = CurrentUserContext.get().getId();
        Expense expense = findExpense(id, userId);
        PaymentMethod paymentMethod = resolvePaymentMethod(request.paymentMethodId(), userId);
        expense.update(
                request.amount(),
                request.category(),
                cleanOptionalText(request.merchantName()),
                paymentMethod,
                request.expenseDate(),
                cleanOptionalText(request.notes())
        );

        return toResponse(expense);
    }

    @Transactional
    public void deleteExpense(Long id) {
        expenseRepository.delete(findExpense(id, CurrentUserContext.get().getId()));
    }

    private Expense findExpense(Long id, Long userId) {
        return expenseRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id " + id));
    }

    private PaymentMethod resolvePaymentMethod(Long paymentMethodId, Long userId) {
        if (paymentMethodId == null) {
            return null;
        }

        return paymentMethodService.findPaymentMethod(paymentMethodId, userId);
    }

    private String cleanOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    ExpenseResponse toResponse(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getUserId(),
                expense.getAmount(),
                expense.getCategory(),
                expense.getMerchantName(),
                expense.getPaymentMethod() == null ? null : paymentMethodService.toResponse(expense.getPaymentMethod()),
                expense.getExpenseDate(),
                expense.getNotes(),
                splitSummary(expense),
                expense.getCreatedAt(),
                expense.getUpdatedAt()
        );
    }

    private ExpenseSplitSummaryResponse splitSummary(Expense expense) {
        return sharedExpenseRepository
                .findByOriginalExpenseIdVisibleToUser(expense.getId(), CurrentUserContext.get().getId())
                .map(sharedExpense -> {
                    SharedExpenseParticipant currentParticipant = sharedExpense.getParticipants().stream()
                            .filter(participant -> participant.getUser().getId().equals(CurrentUserContext.get().getId()))
                            .findFirst()
                            .orElseThrow();
                    User otherUser = sharedExpense.getPaidBy().getId().equals(CurrentUserContext.get().getId())
                            ? sharedExpense.getFriend()
                            : sharedExpense.getPaidBy();
                    String otherUserName = sharedExpense.getPaidBy().getId().equals(CurrentUserContext.get().getId())
                            ? splitFriendLabel(sharedExpense.getParticipants(), CurrentUserContext.get().getId())
                            : otherUser.getName();

                    return new ExpenseSplitSummaryResponse(
                            sharedExpense.getId(),
                            otherUser.getId(),
                            otherUserName,
                            sharedExpense.getTotalAmount(),
                            currentParticipant.getShareAmount(),
                            currentParticipant.getPaidAmount(),
                            currentParticipant.getNetAmount(),
                            SharedExpenseService.displayText(otherUserName, currentParticipant.getNetAmount())
                    );
                })
                .orElse(null);
    }

    private String splitFriendLabel(List<SharedExpenseParticipant> participants, Long currentUserId) {
        List<String> names = participants.stream()
                .map(SharedExpenseParticipant::getUser)
                .filter(user -> !user.getId().equals(currentUserId))
                .map(User::getName)
                .toList();
        if (names.size() == 1) {
            return names.get(0);
        }
        return names.size() + " friends";
    }
}
