package com.example.board.domain.post;

import com.example.board.domain.BaseTimeEntity;
import com.example.board.domain.comment.Comment;
import com.example.board.domain.member.Member;
import com.example.board.domain.team.Team;
import com.example.board.domain.category.TeamCategory;
import com.example.board.domain.teamMember.TeamMember;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Post extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;
    @Lob //TEXT
    private String content;
    @Column(name = "view_count", nullable = false)
    private int viewCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Member author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="team_member_id")
    private TeamMember teamMember;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private TeamCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    @OrderBy("createdDate ASC")
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostImage> images = new ArrayList<>();


    @Builder
    public Post(Long id, String title, String content, Member author, Team team, TeamCategory category, TeamMember teamMember){
        this.id = id;
        this.title = title;
        this.content = content;
        this.author = author;
        this.team = team;
        this.teamMember = teamMember;
        this.category = category;
    }

    public void update(String title, String content, TeamCategory category){
        this.title = title;
        this.content = content;
        this.category = category;
    }

    public void removeImage(PostImage image){
        this.images.remove(image);
        image.setPost(null);
    }

    public void increaseViewCount(){
        this.viewCount ++;
    }

    public void addComment(Comment comment){
        this.comments.add(comment);
        if (comment.getPost() != this){
            comment.setPost(this);
        }
    }

    public void addImage(PostImage image){
        this.images.add(image);
        if (image.getPost() != this)
            image.setPost(this);
    }

    public void clearImages(){
        images.forEach(image -> image.setPost(null));
        images.clear();
    }

}
