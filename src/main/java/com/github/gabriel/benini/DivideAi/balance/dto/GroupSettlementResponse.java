package com.github.gabriel.benini.DivideAi.balance.dto;

import java.util.List;

public record GroupSettlementResponse(
        Long groupId,
        String groupName,
        Long totalAmountInCents,
        Integer participantCount,
        List<ParticipantBalanceResponse> participants,
        List<TransferResponse> transfers
) {}