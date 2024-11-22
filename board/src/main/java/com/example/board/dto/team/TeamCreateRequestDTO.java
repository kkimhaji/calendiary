package com.example.board.dto.team;

import com.example.board.domain.member.Member;
import com.example.board.domain.team.Team;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

public record TeamCreateRequestDTO(
        String teamName,
        String description) {

    public Team toEntity(Member member) {
        return Team.builder()
                .name(this.teamName)
                .description(this.description)
                .created_by(member)
                .createdAt(LocalDateTime.now())
                .build();
    }

}
