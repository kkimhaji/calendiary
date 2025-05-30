package com.example.board.dto.comment;

import com.example.board.domain.member.Member;
import com.example.board.domain.comment.Comment;
import com.example.board.domain.post.Post;
import com.example.board.domain.teamMember.TeamMember;
import jakarta.annotation.Nullable;

public record CreateCommentRequest(
        String content,
        @Nullable Long parentCommentId //대댓글인 경우
//        int depth
) {
}
