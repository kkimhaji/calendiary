package com.example.board.dto.team;

import com.example.board.domain.team.Team;

public record TeamUpdateRequestDTO(
        String teamName,
        String description
) {
}
