package com.example.board.dto.team;

public record InviteValidationResponse(
        Long teamId,
        String teamName,
        String teamDescription,
        boolean isValid,
        String message
) {
}
