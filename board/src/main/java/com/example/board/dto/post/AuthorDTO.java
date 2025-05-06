package com.example.board.dto.post;

import com.example.board.domain.member.Member;
import com.example.board.domain.teamMember.TeamMember;

public record AuthorDTO(
        Long id,
        String username
) {
    public static AuthorDTO from(Member author, TeamMember teamMember){
        String displayName = "Unknown";
        if (author != null) {
            // teamMember가 null이 아닌 경우 teamNickname 사용
            if (teamMember != null) {
                displayName = teamMember.getTeamNickname();
            }
        }
        return new AuthorDTO(author.getMemberId(), displayName);
    }
}
