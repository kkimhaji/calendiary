package com.example.board.member.dto;

public record MemberDeleteRequest(
        String password
) {
    public void validate() {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("비밀번호를 입력해주세요.");
        }
    }
}
