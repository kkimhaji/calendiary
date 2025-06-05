package com.example.board.teamInvite.dto;

public record InviteValidationResponse(
        Long teamId,
        String teamName,
        String teamDescription,
        boolean isValid,
        String message
) {
}
