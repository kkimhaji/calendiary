package com.example.board.team.dto;

public record TeamInfoResponse(
        Long teamId,
        String teamName,
        String teamNickname
) {
}
