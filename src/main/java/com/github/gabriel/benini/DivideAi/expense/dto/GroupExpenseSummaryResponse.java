package com.github.gabriel.benini.DivideAi.expense.dto;

public record GroupExpenseSummaryResponse(
        Long groupId,
        Long totalAmountInCents,
        Integer expenseCount
) {}