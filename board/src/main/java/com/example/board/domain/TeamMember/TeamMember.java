package com.example.board.domain.TeamMember;

import com.example.board.domain.member.Member;
import com.example.board.domain.team.Team;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Builder
@RequiredArgsConstructor
@Getter
@Table(name = "team_members")
public class TeamMember {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Team team;
    @ManyToOne
    private Member member;

//    @ManyToOne
//    private TeamRole role;
    private LocalDateTime joinedAt;


}
