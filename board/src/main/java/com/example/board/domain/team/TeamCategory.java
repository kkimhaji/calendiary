package com.example.board.domain.team;

import com.example.board.domain.role.TeamCategoryRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
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

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private Set<TeamCategoryRole> rolePermissions = new HashSet<>();

}
