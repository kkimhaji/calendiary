package com.example.board.domain.team;

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
    private Long teamId;
    private String name;
    private String description;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Member created_by;
    @CreatedDate
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "team")
    @JsonIgnore
    private Set<TeamMember> members = new HashSet<>();

    @Builder
    public Team(String name, String description, Member created_by, LocalDateTime createdAt){
        this.name = name;
        this.description = description;
        this.created_by = created_by;
        this.createdAt = createdAt;
    }
}
