package com.github.gabriel.benini.DivideAi.balance.dto;

public record TransferResponse(
        Long fromUserId,
        String fromUserName,
        Long toUserId,
        String toUserName,
        Long amountInCents
) {}