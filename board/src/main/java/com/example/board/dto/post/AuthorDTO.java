package com.example.board.dto.post;

import com.example.board.domain.member.Member;
import com.example.board.domain.teamMember.TeamMember;

public record AuthorDTO(
        Long id,
        String username
) {
    public static AuthorDTO from(Member member, TeamMember teamMember){
        return new AuthorDTO(member.getMemberId(), teamMember.getTeamNickname());
    }
}
