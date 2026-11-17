package com.github.gabriel.benini.DivideAi.user.dto;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String name,
        LocalDateTime createdAt
) {}