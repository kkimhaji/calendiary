package com.example.board.service;

import com.example.board.domain.team.Team;
import com.example.board.domain.team.TeamRepository;
import com.example.board.domain.team.TeamRole;
import com.example.board.domain.team.TeamRoleRepository;
import com.example.board.domain.teamMember.TeamMemberRepository;
import com.example.board.dto.role.CreateRoleRequest;
import com.example.board.permission.TeamPermission;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeamRoleService {
    private final TeamRepository teamRepository;
    private final TeamRoleRepository teamRoleRepository;

    public TeamRole createRole(Team team, CreateRoleRequest request){
        if (teamRoleRepository.existsByTeamAndRoleName())
            //DuplicateRoleException으로 나중에 바꿀 것
            throw new RuntimeException("Role name already exists in this team");

        TeamRole role = new TeamRole();
        role.setTeam(team);
        role.setRoleName(request.roleName());
        role.setPermissions(request.permissions());

        return teamRoleRepository.save(role);
    }

    public TeamRole updateRolePermissions(Long roleId, String newPermissions){
        TeamRole role = teamRoleRepository.findById(roleId).orElseThrow(() -> new EntityNotFoundException("Role not found"));

        role.setPermissions(newPermissions);
        return teamRoleRepository.save(role);
    }

    @Transactional(readOnly=true)
    public boolean checkPermission(Long roleId, TeamPermission permission){
        return teamRoleRepository.findById(roleId)
                .map(role -> role.hasPermission(permission))
                .orElse(false);
    }
}
