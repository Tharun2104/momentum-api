# Expense Tracking Runbook

## Purpose

Track the Week 1 and Week 2 implementation work for manual expense tracking.

Source of truth:

- [Personal Finance PRD and Technical Specification](personal-finance-prd-technical-spec.md)

Keep this runbook simple. Use it to show what is complete, what is left, and what must not be pulled into the current phase.

## Current Scope

Week 1:

- Manual expense create, view, edit, delete.
- Monthly summary.
- Category summary.

Week 2:

- Payment method create, view, edit, delete.
- Select payment method when creating or editing an expense.
- Payment method summary.

## Explicitly Not In Scope

- [ ] Geofencing.
- [ ] Location tracking.
- [ ] Location-based notifications.
- [ ] Plaid.
- [ ] Apple Pay integrations.
- [ ] OCR.
- [ ] Splitwise-style flows.
- [ ] Friends or social expenses.
- [ ] AI categorization.

## Backend Tasks

- [x] Add expense and payment method schema migration.
- [x] Add `Expense` entity.
- [x] Add `PaymentMethod` entity.
- [x] Add `ExpenseCategory` enum.
- [x] Add `PaymentMethodType` enum.
- [x] Add expense DTOs.
- [x] Add payment method DTOs.
- [x] Add analytics DTOs.
- [x] Add repositories.
- [x] Add payment method service.
- [x] Add expense service.
- [x] Add finance analytics service.
- [x] Add payment method controller.
- [x] Add expense controller.
- [x] Add analytics controller.
- [x] Add validation annotations.
- [x] Add OpenAPI tags/operation summaries.
- [x] Add service tests.
- [x] Add controller tests.
- [x] Verify focused finance backend tests.
- [x] Verify full backend test suite.

## Flutter Tasks

- [x] Add Riverpod, GoRouter, Dio, Freezed, and generator dependencies.
- [x] Add feature-first finance folder structure.
- [x] Add finance API client layer.
- [x] Add finance repository interfaces and implementations.
- [ ] Convert explicit Dart finance models to generated Freezed models.
- [x] Add Riverpod providers for expenses, payment methods, and analytics.
- [x] Add Expense Dashboard screen.
- [x] Add Expense List screen with pull to refresh and empty state.
- [x] Add Add Expense screen.
- [x] Add Edit Expense screen.
- [x] Add Payment Method List screen.
- [x] Add Add Payment Method screen.
- [x] Add Edit Payment Method screen.
- [x] Add Monthly Summary screen.
- [x] Add GoRouter routes.
- [ ] Add widget tests.
- [ ] Add repository tests.
- [x] Run Flutter analyze and existing tests.

## Current Backend API Checklist

- [x] `POST /api/payment-methods`
- [x] `GET /api/payment-methods`
- [x] `GET /api/payment-methods/{id}`
- [x] `PUT /api/payment-methods/{id}`
- [x] `DELETE /api/payment-methods/{id}`
- [x] `POST /api/expenses`
- [x] `GET /api/expenses`
- [x] `GET /api/expenses/{id}`
- [x] `PUT /api/expenses/{id}`
- [x] `DELETE /api/expenses/{id}`
- [x] `GET /api/analytics/monthly-summary`
- [x] `GET /api/analytics/category-summary`
- [x] `GET /api/analytics/payment-method-summary`

## Notes

- Backend authentication now uses simple email/password login with JWT bearer tokens, and finance APIs resolve ownership from the authenticated local user.
- Payment methods are labels only. They do not connect to banks, cards, Apple Pay, or Plaid.
- The backend is intentionally structured so saved locations, notifications, and shared expenses can be added later without changing the Week 1/2 core model.
