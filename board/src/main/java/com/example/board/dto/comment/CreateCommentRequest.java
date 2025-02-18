package com.example.board.dto.comment;

import com.example.board.domain.member.Member;
import com.example.board.domain.post.Comment;
import com.example.board.domain.post.Post;

import java.util.Optional;

public record CreateCommentRequest(
        String content,
        Optional<Long> parentCommentId //대댓글인 경우
//        int depth
) {
    public Comment toEntity(Post post, Member author, Comment parent){
        return Comment.builder()
                .content(content)
                .post(post)
                .author(author)
                .parent(parent)
//                .depth(depth)
                .build();
    }
}
