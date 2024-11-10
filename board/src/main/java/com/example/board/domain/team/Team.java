package com.example.board.domain.team;

import com.example.board.domain.TeamMember.TeamMember;
import com.example.board.domain.member.Member;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
    private Member created_by;
    @CreatedDate
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "team")
    private Set<TeamMember> members = new HashSet<>();
}
