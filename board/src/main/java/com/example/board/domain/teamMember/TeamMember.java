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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    private TeamMember(Team team, Member member, TeamRole role, String teamNickname, LocalDateTime joinedAt) {
        this.team = team;
        this.member = member;
        this.role = role;
        this.teamNickname = teamNickname;
        this.joinedAt = joinedAt;
    }

    public static TeamMember createTeamMember(Team team, Member member, TeamRole role){
        return new TeamMember(
                team, member, role,
                "ADMIN", LocalDateTime.now()
        );
    }

    public void updateTeamNickname(String teamNickname){
        this.teamNickname = teamNickname;
    }

    //새로운 멤버를 팀에 추가할 때
    public static TeamMember addTeamMember(Team team, Member newMember, TeamRole basicRole, String teamNickname){
        return new TeamMember(
                team, newMember, basicRole,
                teamNickname, LocalDateTime.now()
        );
    }

    public void setRole(TeamRole role){
        this.role = role;
    }

    public void reset(){
        this.role = null;
        this.member = null;
        this.team = null;
    }

    public void updateRole(TeamRole role){
        this.role = role;
    }
}

