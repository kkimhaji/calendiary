package com.example.board.team.dto;

import com.example.board.member.Member;
import com.example.board.team.Team;

import java.time.LocalDateTime;

public record TeamCreateResponse(
        Long teamId,
        String name,
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
