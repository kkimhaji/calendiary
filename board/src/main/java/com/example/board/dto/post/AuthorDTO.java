package com.example.board.dto.post;

import com.example.board.domain.member.Member;

public record AuthorDTO(
        Long id,
        String username
) {
    public static AuthorDTO from(Member member){
        return new AuthorDTO(member.getMemberId(), member.getUsername());
    }
}
