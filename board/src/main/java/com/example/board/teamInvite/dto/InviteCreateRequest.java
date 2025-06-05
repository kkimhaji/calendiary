package com.example.board.teamInvite.dto;

import java.time.LocalDateTime;

public record InviteCreateRequest(
        Long teamId,
        LocalDateTime expiresAt,
        int maxUses
) {}
