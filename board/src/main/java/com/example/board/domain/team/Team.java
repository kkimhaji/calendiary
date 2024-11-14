package com.example.board.domain.team;

import com.example.board.domain.teamMember.TeamMember;
import com.example.board.domain.member.Member;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@RequiredArgsConstructor
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
}
