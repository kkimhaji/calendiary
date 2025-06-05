package com.example.board.team;

import com.example.board.teamMember.TeamMember;
import com.example.board.member.Member;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Team {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Member createdBy;
    @CreatedDate
    private LocalDateTime createdAt;

    private Long basicRoleId;
    private Long adminRoleId;

    @OneToMany(mappedBy = "team")
    @JsonIgnore
    private Set<TeamMember> members = new HashSet<>();

    private Team(String name, String description, Member createdBy, LocalDateTime createdAt) {
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public static Team create(String name, String description, Member member, LocalDateTime createdAt){
        return new Team(name, description, member, createdAt);
    }

    public void updateName(String name){
        if (name != null && !name.isBlank())
            this.name = name;
    }

    public void updateDescription(String description){
        this.description = description;
    }

    public void setBasicRoleId(Long basicRoleId) {
        this.basicRoleId = basicRoleId;
    }

    public void setAdminRoleId(Long adminRoleId) {
        this.adminRoleId = adminRoleId;
    }
}
