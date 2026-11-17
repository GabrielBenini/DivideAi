package com.github.gabriel.benini.DivideAi.balance.dto;

import java.util.List;

public record GroupBalanceResponse(
        Long groupId,
        String groupName,
        Long totalAmountInCents,
        Integer participantCount,
        List<ParticipantBalanceResponse> participants
) {}