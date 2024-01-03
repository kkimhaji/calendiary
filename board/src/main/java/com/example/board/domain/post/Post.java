package com.example.board.domain.post;

import com.example.board.domain.BaseTimeEntity;
import com.example.board.domain.member.Member;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class Post extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;
    @Column(nullable = false)
    private String title;
    @Lob
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member author;

    @Builder
    public Post(Long postId, String title, String content, Member author){
        this.postId = postId;
        this.title = title;
        this.content = content;
        this.author = author;
    }

    public void update(String title, String content){
        this.title = title;
        this.content = content;
    }


}
