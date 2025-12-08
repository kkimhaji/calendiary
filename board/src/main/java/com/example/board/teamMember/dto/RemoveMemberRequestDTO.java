package com.example.board.teamMember.dto;

public record RemoveMemberRequestDTO(
        Boolean deleteContent  // true: 게시글/댓글 삭제, false: 작성자만 null 처리
) {
    public RemoveMemberRequestDTO {
        if (deleteContent == null) {
            deleteContent = false;  // 기본값: 컨텐츠 보존
        }
    }
}