package com.example.board.service;

import com.example.board.domain.member.Member;
import com.example.board.domain.team.TeamRole;
import com.example.board.domain.team.TeamRoleRepository;
import com.example.board.domain.team.Team;
import com.example.board.domain.team.TeamRepository;
import com.example.board.domain.teamMember.TeamMember;
import com.example.board.domain.teamMember.TeamMemberRepository;
import com.example.board.permission.PermissionUtils;
import com.example.board.permission.TeamPermission;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class TeamService {
    private final TeamMemberRepository teamMemberRepository;
    private final TeamRepository teamRepository;
    private final TeamRoleRepository teamRoleRepository;


    @PreAuthorize("hasPermission(#team, 'MANAGE_MEMBERS')")
    public TeamMember addMember(Team team, Member member, TeamRole role){
        TeamMember teamMember = new TeamMember();
        teamMember.setTeam(team);
        teamMember.setMember(member);
        teamMember.setRole(role);
        return teamMemberRepository.save(teamMember);
    }

    @PreAuthorize("hasPermission(#team, 'MANAGE_ROLES')")
    public TeamRole createRole(Team team, String roleName, Set<TeamPermission> permissions) {
        TeamRole role = new TeamRole();
        role.setTeam(team);
        role.setRoleName(roleName);

        String permissionBits = "0";
        for (TeamPermission permission : permissions) {
            permissionBits = PermissionUtils.addPermission(permissionBits, permission);
        }
        role.setPermissions(permissionBits);

        return teamRoleRepository.save(role);
    }

    public Team createTeam(Member member, String teamName, String description){
        Team newTeam = new Team();
        newTeam.setCreated_by(member);
        newTeam.setCreatedAt(LocalDateTime.now());
        newTeam.setName(teamName);
        newTeam.setDescription(description);

        TeamMember teamMember = new TeamMember();
        teamMember.setMember(member);
        //TeamRole = ADMIN(모든 권한)으로 저장
        //teamMember에도 role 저장할 것
        return teamRepository.save(newTeam);
    }
}
