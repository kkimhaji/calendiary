package com.example.board.member.dto;

public record MemberInfoResponse(
        Long memberId,
        String email,
        String nickname
) {
}