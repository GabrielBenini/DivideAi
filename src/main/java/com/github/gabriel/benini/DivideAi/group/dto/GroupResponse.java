package com.github.gabriel.benini.DivideAi.group.dto;

import java.time.LocalDateTime;

public record GroupResponse(
        Long id,
        String name,
        LocalDateTime createdAt
) {}