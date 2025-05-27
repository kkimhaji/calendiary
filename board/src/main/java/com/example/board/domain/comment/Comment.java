package com.example.board.domain.comment;

import com.example.board.domain.BaseTimeEntity;
import com.example.board.domain.member.Member;
import com.example.board.domain.post.Post;
import com.example.board.domain.teamMember.TeamMember;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.parameters.P;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    @JoinColumn(name = "team_member_id")
    private TeamMember teamMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    private int depth;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @OrderBy("createdDate ASC")
    private List<Comment> replies = new ArrayList<>();

    private boolean isDeleted = false;

    private Comment(String content, Post post, Member author, TeamMember teamMember, Comment parent) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be null or empty");
        }
        if (post == null) {
            throw new IllegalArgumentException("Post cannot be null");
        }
        if (author == null) {
            throw new IllegalArgumentException("Author cannot be null");
        }

        this.content = content;
        this.post = post;
        this.author = author;
        this.teamMember = teamMember; // null 허용
        this.parent = parent; // null 허용

        // depth 계산: 부모가 있으면 부모 depth + 1, 없으면 0
        this.depth = (parent != null) ? parent.getDepth() + 1 : 0;

        // 기본값 설정
        this.isDeleted = false;
        this.replies = new ArrayList<>();
    }

    public void setPost(Post post){
        this.post = post;
    }

    public void deleteByAuthor(){
        this.isDeleted = true;
        this.content = "작성자에 의해 삭제된 댓글입니다.";
    }

    public void delete(){
        this.isDeleted = true;
        this.content = "삭제된 댓글입니다.";
    }

    public static Comment createComment(String content,Post post, Member author, TeamMember teamMember, Comment parent){
        return new Comment(content, post, author, teamMember, parent);
    }
}
