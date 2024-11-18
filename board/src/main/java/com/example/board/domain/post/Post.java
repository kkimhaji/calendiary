package com.example.board.domain.post;

import com.example.board.domain.BaseTimeEntity;
import com.example.board.domain.member.Member;
import com.example.board.domain.team.Team;
import com.example.board.domain.team.TeamCategory;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor
public class Post extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;
    @Column(nullable = false)
    private String title;
    @Lob //TEXT
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Member author;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private TeamCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Builder
    public Post(Long postId, String title, String content, Member author, Team team, TeamCategory category){
        this.postId = postId;
        this.title = title;
        this.content = content;
        this.author = author;
        this.team = team;
        this.category = category;
    }

    public void update(String title, String content){
        this.title = title;
        this.content = content;
    }

}
