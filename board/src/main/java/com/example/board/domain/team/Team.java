package com.example.board.domain.team;

import com.example.board.domain.role.TeamRole;
import com.example.board.domain.teamMember.TeamMember;
import com.example.board.domain.member.Member;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Team {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Member created_by;
    @CreatedDate
    private LocalDateTime createdAt;

    private Long basicRoleId;
    private Long adminRoleId;

    @OneToMany(mappedBy = "team")
    @JsonIgnore
    private Set<TeamMember> members = new HashSet<>();

    @Builder
    public Team(String name, String description, Member created_by, LocalDateTime createdAt, Long basicRoleId){
        this.name = name;
        this.description = description;
        this.created_by = created_by;
        this.createdAt = createdAt;
        this.basicRoleId = basicRoleId;
    }

    public void updateName(String name){
        if (name != null && !name.isBlank())
            this.name = name;
    }

    public void updateDescription(String description){
        this.description = description;
    }
}
