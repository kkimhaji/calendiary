package com.example.board.dto.member;

public record MemberInfoResponse(
        Long memberId,
        String email,
        String nickname
) {
}