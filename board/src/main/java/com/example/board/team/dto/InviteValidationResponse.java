package com.example.board.team.dto;

public record InviteValidationResponse(
        Long teamId,
        String teamName,
        String teamDescription,
        boolean isValid,
        String message
) {
}
