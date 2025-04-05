package com.example.board.dto.team;

public record TeamInfoResponse(
        Long teamId,
        String teamName,
        String teamNickname
) {
}
