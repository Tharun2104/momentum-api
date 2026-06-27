package com.mttauto.momentum_api.moneytrack.controller;

import com.mttauto.momentum_api.moneytrack.repository.ExpenseRepository;
import com.mttauto.momentum_api.moneytrack.repository.PaymentMethodRepository;
import com.mttauto.momentum_api.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FinanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private UserRepository userRepository;

    private String tokenUser1;
    private String tokenUser2;
    private Long user1Id;
    private Long user2Id;

    @BeforeEach
    void setUp() throws Exception {
        expenseRepository.deleteAll();
        paymentMethodRepository.deleteAll();
        userRepository.deleteAll();
        tokenUser1 = register("User One", "user1@example.com", "password123");
        tokenUser2 = register("User Two", "user2@example.com", "password123");
        user1Id = userRepository.findByEmail("user1@example.com").orElseThrow().getId();
        user2Id = userRepository.findByEmail("user2@example.com").orElseThrow().getId();
    }

    @Test
    void paymentMethodCrudWorksThroughHttp() throws Exception {
        Long paymentMethodId = createPaymentMethod("Amex", "CREDIT_CARD");

        mockMvc.perform(get("/api/payment-methods/{id}", paymentMethodId)
                        .header("Authorization", bearer(tokenUser1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("Amex"));

        mockMvc.perform(put("/api/payment-methods/{id}", paymentMethodId)
                        .header("Authorization", bearer(tokenUser1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname": "Chase",
                                  "type": "CREDIT_CARD"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("Chase"));

        mockMvc.perform(delete("/api/payment-methods/{id}", paymentMethodId)
                        .header("Authorization", bearer(tokenUser1)))
                .andExpect(status().isNoContent());
    }

    @Test
    void createPaymentMethodReturnsValidationErrorForBlankNickname() throws Exception {
        mockMvc.perform(post("/api/payment-methods")
                        .header("Authorization", bearer(tokenUser1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname": "",
                                  "type": "CREDIT_CARD"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("nickname: nickname is required"));
    }

    @Test
    void expenseCrudAndAnalyticsWorkThroughHttp() throws Exception {
        Long paymentMethodId = createPaymentMethod("Cash", "CASH");
        Long expenseId = createExpense(paymentMethodId, "18.50", "FOOD");

        mockMvc.perform(get("/api/expenses/{id}", expenseId)
                        .header("Authorization", bearer(tokenUser1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.merchantName").value("Starbucks"))
                .andExpect(jsonPath("$.paymentMethod.nickname").value("Cash"));

        mockMvc.perform(put("/api/expenses/{id}", expenseId)
                        .header("Authorization", bearer(tokenUser1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 22.75,
                                  "category": "FOOD",
                                  "merchantName": "Starbucks",
                                  "paymentMethodId": %d,
                                  "expenseDate": "2026-06-20",
                                  "notes": "Coffee and snack"
                                }
                                """.formatted(paymentMethodId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(22.75))
                .andExpect(jsonPath("$.notes").value("Coffee and snack"));

        mockMvc.perform(get("/api/analytics/monthly-summary")
                        .header("Authorization", bearer(tokenUser1))
                        .param("month", "2026-06"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSpent").value(22.75))
                .andExpect(jsonPath("$.transactionCount").value(1))
                .andExpect(jsonPath("$.averageTransactionAmount").value(22.75));

        mockMvc.perform(get("/api/analytics/category-summary")
                        .header("Authorization", bearer(tokenUser1))
                        .param("month", "2026-06"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("FOOD"))
                .andExpect(jsonPath("$[0].totalAmount").value(22.75))
                .andExpect(jsonPath("$[0].percentage").value(100.0));

        mockMvc.perform(get("/api/analytics/payment-method-summary")
                        .header("Authorization", bearer(tokenUser1))
                        .param("month", "2026-06"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].paymentMethod.nickname").value("Cash"))
                .andExpect(jsonPath("$[0].totalAmount").value(22.75));

        mockMvc.perform(delete("/api/expenses/{id}", expenseId)
                        .header("Authorization", bearer(tokenUser1)))
                .andExpect(status().isNoContent());
    }

    @Test
    void createExpenseReturnsValidationErrorForMissingCategory() throws Exception {
        mockMvc.perform(post("/api/expenses")
                        .header("Authorization", bearer(tokenUser1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 18.50,
                                  "merchantName": "Starbucks",
                                  "expenseDate": "2026-06-20"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("category: category is required"));
    }

    @Test
    void createNormalExpenseStillWorksWhenSplitIsMissing() throws Exception {
        Long paymentMethodId = createPaymentMethod("Cash", "CASH");

        mockMvc.perform(post("/api/expenses")
                        .header("Authorization", bearer(tokenUser1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 18.50,
                                  "category": "FOOD",
                                  "merchantName": "Starbucks",
                                  "paymentMethodId": %d,
                                  "expenseDate": "2026-06-20",
                                  "notes": null
                                }
                                """.formatted(paymentMethodId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(18.50));
    }

    @Test
    void createSplitExpenseCreatesPersonalShareAndSharedExpenseForBothUsers() throws Exception {
        Long paymentMethodId = createPaymentMethod("Cash", "CASH");
        acceptFriendship();

        mockMvc.perform(post("/api/expenses")
                        .header("Authorization", bearer(tokenUser1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 40.00,
                                  "category": "FOOD",
                                  "merchantName": "Dinner",
                                  "paymentMethodId": %d,
                                  "expenseDate": "2026-06-21",
                                  "notes": "Dinner with User Two",
                                  "split": {
                                    "enabled": true,
                                    "friendUserId": %d,
                                    "splitType": "EQUAL"
                                  }
                                }
                                """.formatted(paymentMethodId, user2Id)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(20.00));

        mockMvc.perform(get("/api/analytics/monthly-summary")
                        .header("Authorization", bearer(tokenUser1))
                        .param("month", "2026-06"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSpent").value(20.00));

        mockMvc.perform(get("/api/shared-expenses")
                        .header("Authorization", bearer(tokenUser1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Dinner"))
                .andExpect(jsonPath("$[0].totalAmount").value(40.00))
                .andExpect(jsonPath("$[0].participants.length()").value(2))
                .andExpect(jsonPath("$[0].participants[0].shareAmount").value(20.00))
                .andExpect(jsonPath("$[0].participants[0].paidAmount").value(40.00))
                .andExpect(jsonPath("$[0].participants[0].netAmount").value(20.00))
                .andExpect(jsonPath("$[0].participants[1].shareAmount").value(20.00))
                .andExpect(jsonPath("$[0].participants[1].paidAmount").value(0.00))
                .andExpect(jsonPath("$[0].participants[1].netAmount").value(-20.00));

        mockMvc.perform(get("/api/shared-expenses")
                        .header("Authorization", bearer(tokenUser2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Dinner"));
    }

    @Test
    void createSplitExpenseCanSplitWithMultipleFriendsThroughHttp() throws Exception {
        Long paymentMethodId = createPaymentMethod("Cash", "CASH");
        acceptFriendship();
        Long user3Id = registerAndFindId("User Three", "user3@example.com");
        String tokenUser3 = login("user3@example.com", "password123");
        Long requestId = sendFriendRequest(user3Id, tokenUser1);
        mockMvc.perform(post("/api/friends/requests/{requestId}/accept", requestId)
                        .header("Authorization", bearer(tokenUser3)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/expenses")
                        .header("Authorization", bearer(tokenUser1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 90.00,
                                  "category": "FOOD",
                                  "merchantName": "Dinner",
                                  "paymentMethodId": %d,
                                  "expenseDate": "2026-06-21",
                                  "notes": "Group dinner",
                                  "split": {
                                    "enabled": true,
                                    "friendUserIds": [%d, %d],
                                    "splitType": "EQUAL"
                                  }
                                }
                                """.formatted(paymentMethodId, user2Id, user3Id)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(30.00));

        mockMvc.perform(get("/api/shared-expenses")
                        .header("Authorization", bearer(tokenUser1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].participants.length()").value(3))
                .andExpect(jsonPath("$[0].currentUserShareAmount").value(30.00))
                .andExpect(jsonPath("$[0].currentUserNetAmount").value(60.00));
    }

    @Test
    void deleteSharedExpenseRemovesSplitAndLinkedExpenseThroughHttp() throws Exception {
        Long paymentMethodId = createPaymentMethod("Cash", "CASH");
        acceptFriendship();

        mockMvc.perform(post("/api/expenses")
                        .header("Authorization", bearer(tokenUser1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(splitExpenseJson(paymentMethodId, user2Id)))
                .andExpect(status().isCreated());

        String sharedExpenses = mockMvc.perform(get("/api/shared-expenses")
                        .header("Authorization", bearer(tokenUser1)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long sharedExpenseId = Long.valueOf(sharedExpenses.substring(
                sharedExpenses.indexOf("\"id\":") + 5,
                sharedExpenses.indexOf(",", sharedExpenses.indexOf("\"id\":"))
        ));

        mockMvc.perform(delete("/api/shared-expenses/{id}", sharedExpenseId)
                        .header("Authorization", bearer(tokenUser1)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/shared-expenses")
                        .header("Authorization", bearer(tokenUser1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(get("/api/analytics/monthly-summary")
                        .header("Authorization", bearer(tokenUser1))
                        .param("month", "2026-06"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSpent").value(0));
    }

    @Test
    void createSplitExpenseRejectsInvalidFriendCases() throws Exception {
        Long paymentMethodId = createPaymentMethod("Cash", "CASH");

        mockMvc.perform(post("/api/expenses")
                        .header("Authorization", bearer(tokenUser1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(splitExpenseJson(paymentMethodId, user1Id)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot split an expense with yourself"));

        mockMvc.perform(post("/api/expenses")
                        .header("Authorization", bearer(tokenUser1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(splitExpenseJson(paymentMethodId, user2Id)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Friend relationship must be accepted"));
    }

    @Test
    void createSplitExpenseRejectsPendingFriendship() throws Exception {
        Long paymentMethodId = createPaymentMethod("Cash", "CASH");
        sendFriendRequest();

        mockMvc.perform(post("/api/expenses")
                        .header("Authorization", bearer(tokenUser1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(splitExpenseJson(paymentMethodId, user2Id)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Friend relationship must be accepted"));
    }

    @Test
    void createSplitExpenseRejectsMissingFriend() throws Exception {
        Long paymentMethodId = createPaymentMethod("Cash", "CASH");
        Long missingUserId = 99999L;

        mockMvc.perform(post("/api/expenses")
                        .header("Authorization", bearer(tokenUser1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(splitExpenseJson(paymentMethodId, missingUserId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with id " + missingUserId));
    }

    @Test
    void createSplitExpenseRejectsPaymentMethodOwnedByAnotherUser() throws Exception {
        Long paymentMethodId = createPaymentMethodForUser(tokenUser2, "Other Cash", "CASH");
        acceptFriendship();

        mockMvc.perform(post("/api/expenses")
                        .header("Authorization", bearer(tokenUser1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(splitExpenseJson(paymentMethodId, user2Id)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Payment method not found with id " + paymentMethodId));
    }

    @Test
    void sharedExpenseDetailIsOnlyVisibleToParticipants() throws Exception {
        Long paymentMethodId = createPaymentMethod("Cash", "CASH");
        acceptFriendship();
        Long user3Id = registerAndFindId("User Three", "user3@example.com");
        String tokenUser3 = login("user3@example.com", "password123");

        mockMvc.perform(post("/api/expenses")
                        .header("Authorization", bearer(tokenUser1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(splitExpenseJson(paymentMethodId, user2Id)))
                .andExpect(status().isCreated());

        String sharedExpenses = mockMvc.perform(get("/api/shared-expenses")
                        .header("Authorization", bearer(tokenUser1)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long sharedExpenseId = Long.valueOf(sharedExpenses.substring(
                sharedExpenses.indexOf("\"id\":") + 5,
                sharedExpenses.indexOf(",", sharedExpenses.indexOf("\"id\":"))
        ));

        mockMvc.perform(get("/api/shared-expenses/{id}", sharedExpenseId)
                        .header("Authorization", bearer(tokenUser2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sharedExpenseId));

        mockMvc.perform(get("/api/shared-expenses/{id}", sharedExpenseId)
                        .header("Authorization", bearer(tokenUser3)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getExpensesFiltersByMonthAndPaymentMethod() throws Exception {
        Long amexId = createPaymentMethod("Amex", "CREDIT_CARD");
        Long cashId = createPaymentMethod("Cash", "CASH");

        createExpense(amexId, "18.50", "FOOD", "Starbucks", "2026-06-20");
        createExpense(amexId, "44.25", "GROCERIES", "Walmart", "2026-06-21");
        createExpense(cashId, "9.00", "FOOD", "Cafe", "2026-06-21");
        createExpense(amexId, "100.00", "SHOPPING", "Target", "2026-05-15");

        mockMvc.perform(get("/api/expenses")
                        .header("Authorization", bearer(tokenUser1))
                        .param("month", "2026-06")
                        .param("paymentMethodId", amexId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].merchantName").value("Walmart"))
                .andExpect(jsonPath("$[0].paymentMethod.nickname").value("Amex"))
                .andExpect(jsonPath("$[1].merchantName").value("Starbucks"))
                .andExpect(jsonPath("$[1].paymentMethod.nickname").value("Amex"));
    }

    @Test
    void usersCannotAccessEachOthersFinanceData() throws Exception {
        Long paymentMethodId = createPaymentMethod("Cash", "CASH");
        Long expenseId = createExpense(paymentMethodId, "18.50", "FOOD");

        mockMvc.perform(get("/api/payment-methods/{id}", paymentMethodId)
                        .header("Authorization", bearer(tokenUser2)))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/expenses/{id}", expenseId)
                        .header("Authorization", bearer(tokenUser2)))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/analytics/monthly-summary")
                        .header("Authorization", bearer(tokenUser2))
                        .param("month", "2026-06"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSpent").value(0))
                .andExpect(jsonPath("$.transactionCount").value(0));
    }

    @Test
    void protectedFinanceApisRequireJwtToken() throws Exception {
        mockMvc.perform(get("/api/expenses"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/payment-methods"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/analytics/monthly-summary")
                        .param("month", "2026-06"))
                .andExpect(status().isUnauthorized());
    }

    private Long createPaymentMethod(String nickname, String type) throws Exception {
        return createPaymentMethodForUser(tokenUser1, nickname, type);
    }

    private Long createPaymentMethodForUser(String token, String nickname, String type) throws Exception {
        mockMvc.perform(post("/api/payment-methods")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname": "%s",
                                  "type": "%s"
                                }
                                """.formatted(nickname, type)))
                .andExpect(status().isCreated());

        Long ownerId = token.equals(tokenUser1) ? user1Id : user2Id;
        return paymentMethodRepository.findByUser_IdOrderByCreatedAtDesc(ownerId).stream()
                .filter(paymentMethod -> paymentMethod.getNickname().equals(nickname))
                .findFirst()
                .orElseThrow()
                .getId();
    }

    private String splitExpenseJson(Long paymentMethodId, Long friendUserId) {
        return """
                {
                  "amount": 40.00,
                  "category": "FOOD",
                  "merchantName": "Dinner",
                  "paymentMethodId": %d,
                  "expenseDate": "2026-06-21",
                  "notes": "Dinner",
                  "split": {
                    "enabled": true,
                    "friendUserId": %d,
                    "splitType": "EQUAL"
                  }
                }
                """.formatted(paymentMethodId, friendUserId);
    }

    private void acceptFriendship() throws Exception {
        Long requestId = sendFriendRequest();
        mockMvc.perform(post("/api/friends/requests/{requestId}/accept", requestId)
                        .header("Authorization", bearer(tokenUser2)))
                .andExpect(status().isOk());
    }

    private Long sendFriendRequest() throws Exception {
        return sendFriendRequest(user2Id, tokenUser1);
    }

    private Long sendFriendRequest(Long receiverUserId, String token) throws Exception {
        String body = mockMvc.perform(post("/api/friends/request")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "receiverUserId": %d
                                }
                                """.formatted(receiverUserId)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return Long.valueOf(body.substring(body.indexOf("\"id\":") + 5, body.indexOf(",")));
    }

    private Long registerAndFindId(String name, String email) throws Exception {
        register(name, email, "password123");
        return userRepository.findByEmail(email).orElseThrow().getId();
    }

    private String login(String email, String password) throws Exception {
        String body = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return body.substring(body.indexOf(":\"") + 2, body.indexOf("\","));
    }

    private Long createExpense(Long paymentMethodId, String amount, String category) throws Exception {
        return createExpense(paymentMethodId, amount, category, "Starbucks", "2026-06-20");
    }

    private Long createExpense(
            Long paymentMethodId,
            String amount,
            String category,
            String merchantName,
            String expenseDate
    ) throws Exception {
        mockMvc.perform(post("/api/expenses")
                        .header("Authorization", bearer(tokenUser1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": %s,
                                  "category": "%s",
                                  "merchantName": "%s",
                                  "paymentMethodId": %d,
                                  "expenseDate": "%s",
                                  "notes": null
                                }
                                """.formatted(amount, category, merchantName, paymentMethodId, expenseDate)))
                .andExpect(status().isCreated());

        return expenseRepository.findAll().get(0).getId();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String register(String name, String email, String password) throws Exception {
        String body = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(name, email, password)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return body.substring(body.indexOf(":\"") + 2, body.indexOf("\","));
    }
}
