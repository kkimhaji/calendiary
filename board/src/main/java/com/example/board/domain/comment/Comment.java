package com.example.board.domain.comment;

import com.example.board.domain.BaseTimeEntity;
import com.example.board.domain.member.Member;
import com.example.board.domain.post.Post;
import com.example.board.domain.teamMember.TeamMember;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.parameters.P;

import java.util.ArrayList;
import java.util.List;

@Getter
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
    @JoinColumn(name = "team_member_id")
    private TeamMember teamMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    private int depth;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Comment> replies = new ArrayList<>();

    private boolean isDeleted = false;

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
}
