package com.example.board.post;

import com.example.board.common.domain.BaseContentEntity;
import com.example.board.comment.Comment;
import com.example.board.common.exception.PostValidationException;
import com.example.board.member.Member;
import com.example.board.team.Team;
import com.example.board.category.TeamCategory;
import com.example.board.teamMember.TeamMember;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseContentEntity {
    @Column(name = "view_count", nullable = false)
    private int viewCount = 0;

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

    private Post(String title, String content, Member author, TeamCategory category, Team team, TeamMember teamMember) {

        super(title, content, author);          // 공통 필드 초기화

        validate(title, content, author, category, team);

        this.category   = category;
        this.team       = team;
        this.teamMember = teamMember;
    }

    public static Post create(String title, String content, Member author, TeamCategory category, Team team, TeamMember teamMember) {
        return new Post(title, content, author, category, team, teamMember);
    }

    public void update(String title, String content, TeamCategory category){

        validate(title, content, getAuthor(), category, getTeam());

        changeTitleAndContent(title, content);  // 부모 메서드로 변경
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

    private static void validate(String title, String content,
                                 Member author, TeamCategory category, Team team) {

        if (title == null || title.trim().isEmpty()) {
            throw new PostValidationException("Title cannot be null or empty.");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new PostValidationException("Content cannot be null or empty.");
        }
        if (author == null) {
            throw new PostValidationException("Author cannot be null.");
        }
        if (category == null) {
            throw new PostValidationException("Category cannot be null.");
        }
        if (team == null) {
            throw new PostValidationException("Team cannot be null.");
        }
    }

    public void setTeamMember(TeamMember teamMember) {
        this.teamMember = teamMember;
    }
}