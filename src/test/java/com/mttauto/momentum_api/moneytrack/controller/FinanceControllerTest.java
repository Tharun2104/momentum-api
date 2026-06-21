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

    @BeforeEach
    void setUp() throws Exception {
        expenseRepository.deleteAll();
        paymentMethodRepository.deleteAll();
        userRepository.deleteAll();
        tokenUser1 = register("User One", "user1@example.com", "password123");
        tokenUser2 = register("User Two", "user2@example.com", "password123");
        user1Id = userRepository.findByEmail("user1@example.com").orElseThrow().getId();
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
        mockMvc.perform(post("/api/payment-methods")
                        .header("Authorization", bearer(tokenUser1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname": "%s",
                                  "type": "%s"
                                }
                                """.formatted(nickname, type)))
                .andExpect(status().isCreated());

        return paymentMethodRepository.findByUser_IdOrderByCreatedAtDesc(user1Id).stream()
                .filter(paymentMethod -> paymentMethod.getNickname().equals(nickname))
                .findFirst()
                .orElseThrow()
                .getId();
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
