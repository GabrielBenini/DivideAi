package com.github.gabriel.benini.DivideAi.balance.dto;

public record ParticipantBalanceResponse(
        Long userId,
        String userName,
        Long totalPaidInCents,
        Long shareAmountInCents,
        Long balanceInCents
) {}