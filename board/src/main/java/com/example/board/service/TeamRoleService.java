package com.example.board.service;

import com.example.board.domain.team.Team;
import com.example.board.domain.team.TeamRepository;
import com.example.board.domain.role.TeamRole;
import com.example.board.domain.role.TeamRoleRepository;
import com.example.board.dto.role.CreateRoleRequest;
import com.example.board.permission.TeamPermission;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.example.board.permission.TeamPermission.*;

@Service
@RequiredArgsConstructor
public class TeamRoleService {
    private final TeamRepository teamRepository;
    private final TeamRoleRepository teamRoleRepository;

    //    public TeamRole createRole(Team team, CreateRoleRequest request){
//        if (teamRoleRepository.existsByTeamAndRoleName())
//            //DuplicateRoleNameException 나중에 바꿀 것
//            throw new RuntimeException("Role name already exists in this team");
//
//        TeamRole role = new TeamRole();
//        role.setTeam(team);
//        role.setRoleName(request.roleName());
//        role.setPermissions(request.permissions());
//
//        return teamRoleRepository.save(role);
//    }
//
//    public TeamRole updateRolePermissions(Long roleId, String newPermissions){
//        TeamRole role = teamRoleRepository.findById(roleId).orElseThrow(() -> new EntityNotFoundException("Role not found"));
//
//        role.setPermissions(newPermissions);
//        return teamRoleRepository.save(role);
//    }
//
//    @Transactional(readOnly=true)
//    public boolean checkPermission(Long roleId, TeamPermission permission){
//        return teamRoleRepository.findById(roleId)
//                .map(role -> role.hasPermission(permission))
//                .orElse(false);
//    }

    public TeamRole createRole(Long teamId, CreateRoleRequest request) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found"));

        // 역할 이름 중복 검사
        if (teamRoleRepository.existsByTeamAndRoleName(team, request.roleName())) {
            throw new RuntimeException("Role name already exists in this team");
        }

        TeamRole role = new TeamRole();
        role.setTeam(team);
        role.setRoleName(request.roleName());
        role.setPermissions(request.permissions());  // 내부적으로 비트셋으로 변환
        role.setDescription(request.description());
        return teamRoleRepository.save(role);
    }

    public TeamRole updateRolePermissions(Long roleId, Set<TeamPermission> newPermissions) {
        TeamRole role = teamRoleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));

        role.setPermissions(newPermissions);
        return teamRoleRepository.save(role);
    }

    public boolean checkPermission(Long roleId, TeamPermission permission) {
        return teamRoleRepository.findById(roleId)
                .map(role -> role.hasPermission(permission))
                .orElse(false);
    }
    public TeamRole getRoleById(Long roleId){
        var role = teamRoleRepository.findById(roleId);
        if (role.isEmpty())
            throw new EntityNotFoundException("That is not proper roleId");
        return role.get();
    }

    public TeamRole createAdmin(Team team){

        Set<TeamPermission> adminPermissions = new HashSet<>(Arrays.asList(
                CREATE_POST, DELETE_POST, ADMINISTRATOR,
                MANAGE_ROLES, EDIT_POST, MANAGE_MEMBERS,
                VIEW_POST
        ));

        CreateRoleRequest request = new CreateRoleRequest("ADMIN", adminPermissions, "who made this team");
        return createRole(team.getTeamId(), request);

    }

}
