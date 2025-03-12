package com.example.board.dto.team;

import com.example.board.domain.member.Member;
import com.example.board.domain.role.TeamRole;
import com.example.board.domain.team.Team;

public record AddMemberRequestDTO(
        Long teamId,
        Long memberId
) {

}
