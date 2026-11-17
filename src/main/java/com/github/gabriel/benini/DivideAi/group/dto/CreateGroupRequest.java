package com.github.gabriel.benini.DivideAi.group.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateGroupRequest(

        @NotBlank(message = "Group name is required")
        @Size(max = 100, message = "Group name must not exceed 100 characters")
        String name,

        @NotNull(message = "Group creator is required")
        @Positive(message = "Creator ID must be positive")
        Long createdByUserId

) {}