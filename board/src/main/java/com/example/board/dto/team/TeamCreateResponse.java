package com.example.board.dto.team;

import com.example.board.domain.member.Member;
import com.example.board.domain.team.Team;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class TeamCreateResponse {
    private String teamName;
    private String description;
    private LocalDateTime createdAt;
    private Member createdBy;

    public TeamCreateResponse fromEntity(Team team){
        return TeamCreateResponse.builder().teamName(team.getName())
                .description(team.getDescription())
                .createdAt(team.getCreatedAt())
                .createdBy(team.getCreated_by())
                .build();
    }

}
