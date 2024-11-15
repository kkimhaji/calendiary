package com.example.board.dto.team;

import com.example.board.domain.member.Member;
import com.example.board.domain.team.Team;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TeamCreateRequestDTO {

    private String teamName;
    private String description;

    public Team toEntity(Member member) {
        return Team.builder()
                .name(teamName)
                .description(description)
                .created_by(member)
                .createdAt(LocalDateTime.now())
                .build();
    }

}
