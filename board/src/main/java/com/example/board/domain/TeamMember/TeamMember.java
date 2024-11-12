package com.example.board.domain.teamMember;

import com.example.board.domain.member.Member;
import com.example.board.domain.role.TeamRole;
import com.example.board.domain.team.Team;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Builder
@RequiredArgsConstructor
@Getter
@Table(name = "team_members")
@Setter
public class TeamMember {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private TeamRole role;
    private String teamNickname;

    private LocalDateTime joinedAt;

}
