package com.example.board.post.dto;

import com.example.board.teamMember.TeamMember;

public record AuthorDTO(
        Long id,
        String username
) {
    public static AuthorDTO from(TeamMember author) {
        String displayName = "Unknown";
        Long authorId = null;
        if (author != null) {
            // teamMember가 null이 아닌 경우 teamNickname 사용
            displayName = author.getTeamNickname();
            authorId = author.getId();
        }

        return new AuthorDTO(authorId, displayName);
    }
}
