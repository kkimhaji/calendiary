package com.example.board.domain.team;

import com.example.board.domain.post.Post;
import com.example.board.domain.role.TeamCategoryRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@Setter
public class TeamCategory {
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String name;
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @OneToMany(mappedBy = "category")
    private List<Post> posts = new ArrayList<>();//인덱스 접근이 필요함(페이징처리)

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)

    private Set<TeamCategoryRole> rolePermissions = new HashSet<>();
    public void addPost(Post post) {
        this.posts.add(post);
        if (post.getCategory() != this) {
            post.setCategory(this);
        }
    }
}
