package com.example.board.dto;

import com.example.board.domain.post.Post;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class PostListResponseDto {
    private Long postId;
    private String title;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;

    public PostListResponseDto(Post post){
        this.postId = post.getPostId();
        this.title = post.getTitle();
        this.createDate = post.getCreatedDate();
        this.updateDate = post.getModifiedDate();
    }


}
