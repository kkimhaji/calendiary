package com.example.board.dto.comment;

import com.example.board.domain.member.Member;
import com.example.board.domain.post.Comment;
import com.example.board.domain.post.Post;
import com.example.board.domain.teamMember.TeamMember;
import jakarta.annotation.Nullable;

import java.util.Optional;

public record CreateCommentRequest(
        String content,
        @Nullable Long parentCommentId //대댓글인 경우
//        int depth
) {
    public Comment toEntity(Post post, Member author, Comment parent, TeamMember teamMember){
        return Comment.builder()
                .content(content)
                .post(post)
                .author(author)
                .teamMember(teamMember)
                .parent(parent)
//                .depth(depth)
                .build();
    }
}
