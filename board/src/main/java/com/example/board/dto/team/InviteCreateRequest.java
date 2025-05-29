package com.example.board.dto.team;

import com.example.board.domain.team.TeamInvite;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record InviteCreateRequest(
        Long teamId,
        LocalDateTime expiresAt,
        int maxUses
) {}
