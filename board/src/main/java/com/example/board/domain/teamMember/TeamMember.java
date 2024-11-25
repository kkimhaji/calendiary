package com.example.board.domain.teamMember;

import com.example.board.domain.member.Member;
import com.example.board.domain.role.TeamRole;
import com.example.board.domain.team.Team;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "team_members")
@Setter
@NoArgsConstructor
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

    public void createTeam(Team team, Member member, TeamRole role){
        this.member = member;
        this.team = team;
        this.role = role;
        this.teamNickname = "ADMIN";
        this.joinedAt = LocalDateTime.now();
    }

//    @Builder
//    public TeamMember(Team team, Member member, TeamRole role, String teamNickname, LocalDateTime joinedAt) {
//        this.team = team;
//        this.member = member;
//        this.role = role;
//        this.teamNickname = teamNickname;
//        this.joinedAt = joinedAt;
//    }
}

