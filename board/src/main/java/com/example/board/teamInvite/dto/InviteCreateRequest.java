package com.example.board.teamInvite.dto;

import java.time.LocalDateTime;

public record InviteCreateRequest(
        LocalDateTime expiresAt,
        int maxUses
) {}
