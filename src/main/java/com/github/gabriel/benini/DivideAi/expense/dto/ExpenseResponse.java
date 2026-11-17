package com.github.gabriel.benini.DivideAi.expense.dto;

import java.time.LocalDateTime;

public record ExpenseResponse(
        Long id,
        String description,
        Long amountInCents,
        Long paidByUserId,
        String paidByUserName,
        Long groupId,
        LocalDateTime createdAt
) {}