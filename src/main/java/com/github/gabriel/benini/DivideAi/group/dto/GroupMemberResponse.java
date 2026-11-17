package com.github.gabriel.benini.DivideAi.group.dto;

import java.time.LocalDateTime;

public record GroupMemberResponse(
        Long id,
        Long userId,
        String userName,
        LocalDateTime joinedAt
) {}