package com.example.board.dto;

import com.example.board.domain.member.Member;
import com.example.board.domain.post.Post;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostSaveRequestDto {
    private String title;
    private String content;
    private Member author;

    @Builder
    public PostSaveRequestDto(String title, String content, Member author){
        this.title = title;
        this.content = content;
        this.author = author;
    }

    public Post toEntity(){
        return Post.builder().title(title).content(content).author(author).build();
    }

}
