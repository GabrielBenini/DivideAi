package com.github.gabriel.benini.DivideAi.expense.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateExpenseRequest(

        @NotBlank(message = "Description is required")
        @Size(max = 255, message = "Description must not exceed 255 characters")
        String description,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be greater than zero")
        Long amountInCents,

        @NotNull(message = "Payer ID is required")
        @Positive(message = "Payer ID must be positive")
        Long paidByUserId,

        @NotNull(message = "Group ID is required")
        @Positive(message = "Group ID must be positive")
        Long groupId

) {}