package com.example.board.domain.post;

import com.example.board.domain.BaseTimeEntity;
import com.example.board.domain.member.Member;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class Comment extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Member author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Comment> replies = new ArrayList<>();

    private boolean isDeleted = false;

    @Builder
    public Comment(String content, Post post, Member author, Comment parent) {
        this.content = content;
        this.post = post;
        this.author = author;
        this.parent = parent;
    }

    public void deleteByAuthor(){
        this.isDeleted = true;
        this.content = "작성자에 의해 삭제된 댓글입니다.";
    }

    public void delete(){
        this.isDeleted = true;
        this.content = "삭제된 댓글입니다.";
    }
}
