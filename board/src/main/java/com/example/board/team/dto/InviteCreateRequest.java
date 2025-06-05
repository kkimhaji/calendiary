package com.example.board.team.dto;

import java.time.LocalDateTime;

public record InviteCreateRequest(
        Long teamId,
        LocalDateTime expiresAt,
        int maxUses
) {}
