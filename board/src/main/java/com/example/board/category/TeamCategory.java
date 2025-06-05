package com.example.board.category;

import com.example.board.post.Post;
import com.example.board.role.CategoryRolePermission;
import com.example.board.team.Team;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamCategory {
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String name;
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @OneToMany(mappedBy = "category")
    private Set<CategoryRolePermission> rolePermissions = new HashSet<>();

    private TeamCategory(String name, String description, Team team) {
        this.name = name;
        this.description = description;
        this.team = team;
    }

    public void addPost(Post post) {
//        this.posts.add(post);
        if (post.getCategory() != this) {
            post.setCategory(this);
        }
    }

    public void clearRolePermissions(){
        this.rolePermissions.clear();
    }
    public void updateName(String name){
        this.name = name;
    }

    public void updateDescription(String description){
        this.description = description;
    }

    public void addRolePermission(CategoryRolePermission permission){
        this.rolePermissions.add(permission);
        if (permission.getCategory()!=this)
            permission.setCategory(this);
    }

    public static TeamCategory createCategory(String name, String description, Team team) {
        return new TeamCategory(name, description, team);
    }
}
