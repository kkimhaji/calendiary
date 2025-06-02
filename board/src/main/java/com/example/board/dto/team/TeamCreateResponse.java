package com.example.board.dto.team;

import com.example.board.domain.member.Member;
import com.example.board.domain.team.Team;

import java.time.LocalDateTime;

public record TeamCreateResponse(
        Long teamId,
        String teamName,
        String description,
        LocalDateTime createdAt,
        Member createdBy
) {
    public static TeamCreateResponse fromEntity(Team team) {
        return new TeamCreateResponse(
                team.getId(),
                team.getName(),
                team.getDescription(),
                team.getCreatedAt(),
                team.getCreatedBy()
        );
    }

}
