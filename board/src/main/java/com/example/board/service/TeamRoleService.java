package com.example.board.service;

import com.example.board.domain.member.Member;
import com.example.board.domain.role.CategoryPermissionRepository;
import com.example.board.domain.role.CategoryRolePermission;
import com.example.board.domain.team.Team;
import com.example.board.domain.team.TeamRepository;
import com.example.board.domain.role.TeamRole;
import com.example.board.domain.role.TeamRoleRepository;
import com.example.board.domain.teamMember.TeamMember;
import com.example.board.domain.teamMember.TeamMemberRepository;
import com.example.board.dto.teamMember.TeamMemberOfRoleDTO;
import com.example.board.dto.role.*;
import com.example.board.exception.RoleDeletionException;
import com.example.board.permission.*;
import com.example.board.permission.evaluator.TeamPermissionEvaluator;
import com.example.board.permission.utils.PermissionUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamRoleService {
    private final TeamRepository teamRepository;
    private final TeamRoleRepository teamRoleRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final CategoryPermissionRepository categoryPermissionRepository;
    private final TeamMemberService teamMemberService;
    private final TeamPermissionEvaluator teamPermissionEvaluator;

    public TeamRole createRole(Long teamId, CreateRoleRequest request) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found"));

        // 역할 이름 중복 검사
        if (teamRoleRepository.existsByTeamAndRoleName(team, request.roleName())) {
            throw new RuntimeException("Role name already exists in this team");
        }
        TeamRole newRole = teamRoleRepository.save(request.toEntity(team));
        //category role permission에 기본 저장
        int insertedRows = categoryPermissionRepository.createDefaultPermissionsForNewRole(teamId, newRole.getId());

        // 4. (선택) 결과 확인
        if(insertedRows == 0) {
            throw new IllegalStateException("카테고리가 존재하지 않아 권한을 생성할 수 없습니다");
        }

        return newRole;
    }

    public TeamRole updateRolePermissions(Long roleId, Set<TeamPermission> newPermissions) {
        TeamRole role = teamRoleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));
        role.setPermissions(newPermissions);
        return teamRoleRepository.save(role);
    }

    //teamPermission 체크 //test에서만 사용하므로 삭제할 것
    public boolean checkPermission(Long roleId, TeamPermission permission) {
        return teamRoleRepository.findById(roleId)
                .map(role -> role.hasPermission(permission))
                .orElseThrow(()-> new AccessDeniedException("권한이 없습니다."));
    }

    public TeamRole getRoleById(Long roleId){
        var role = teamRoleRepository.findById(roleId);
        if (role.isEmpty())
            throw new EntityNotFoundException("That is not proper roleId");
        return role.get();
    }

    public TeamRole createAdmin(Team team){
        Set<TeamPermission> adminPermissions = new HashSet<>(Arrays.asList(TeamPermission.values()));
        CreateRoleRequest request = new CreateRoleRequest("ADMIN", adminPermissions, "who made this team");
        return createRole(team.getId(), request);
    }

    public TeamRole createBasic(Team team){
        return createRole(team.getId(), new CreateRoleRequest("Member", new HashSet<>(List.of()), "member of this team"));
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long teamId, Long roleId){
        try {
            //role 삭제 -> 기존 멤버들: default role로 변경
            // TeamMember 수정, Category의 role도 삭제
            TeamRole targetRole = teamRoleRepository.findById(roleId)
                    .orElseThrow(() -> new EntityNotFoundException("role not found"));
            Team team = teamRepository.findById(teamId).orElseThrow(() -> new EntityNotFoundException("no such team"));
            Long basicRoleId = team.getBasicRoleId();

            if (roleId.equals(basicRoleId))
                throw new IllegalStateException("기본 역할은 삭제할 수 없습니다.");

            // defaultRole로 변경
            updateMembersRole(team, targetRole);

            deleteCategoryPermissions(roleId);
            teamRoleRepository.deleteById(roleId);
        }
        catch (Exception e){
            throw new RoleDeletionException("역할 삭제 중 오류가 발생했습니다.");
        }
    }

    //특정 역할의 멤버들 역할을 일괄적으로 기본 역할로 변경 (역할을 삭제하기 전에 사용)
    private void updateMembersRole(Team team, TeamRole targetRole){
        TeamRole basicRole = teamRoleRepository.findById(team.getBasicRoleId())
                .orElseThrow(() -> new EntityNotFoundException("role not found"));

        List<TeamMember> membersWithRole = teamMemberRepository.findAllByTeamAndRole(team, targetRole);

        membersWithRole.forEach(member -> member.setRole(basicRole));
        teamMemberRepository.saveAll(membersWithRole);
    }

    private void deleteCategoryPermissions(Long roleId){
        categoryPermissionRepository.deleteAllByRoleId(roleId);
    }

    @Transactional
    //역할에 멤버 추가
    public AddMembersToRoleResponse addMemberToRole(Long teamId, AddMembersToRoleRequest request){
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("team not found"));
        TeamRole role = teamRoleRepository.findById(request.roleId())
                .orElseThrow(()-> new EntityNotFoundException("role not found"));

        List<TeamMember> targetMembers = teamMemberRepository.findAllByTeamAndMemberIdIn(team, request.members());

        if (targetMembers.size() != request.members().size())
            throw new IllegalArgumentException("일부 멤버가 팀에 속해있지 않습니다.");

        List<String> membersName = new ArrayList<>();
        targetMembers.forEach(member -> {
            member.setRole(role);
            membersName.add(member.getTeamNickname());
        });
        teamMemberRepository.saveAll(targetMembers);

        return new AddMembersToRoleResponse(role.getRoleName(), membersName);
    }

    @Transactional
    //역할에서 멤버 삭제하는 기능 추가할 것 - '역할'기준으로
    public void removeMemberFromRole(Long teamId, Long memberId, Long newRoleId){
        //빼는 멤버는 기본 역할 or 원하는 역할로
        if (newRoleId != null){
            changeMemberRole(teamId, memberId, newRoleId);
        }else{
            //역할 지정 안 된 경우엔 기본 역할로
            changeToDefaultRole(teamId, memberId);
        }
    }
    // 특정 역할로 변경
    private void changeMemberRole(Long teamId, Long memberId, Long newRoleId) {
        TeamMember teamMember = teamMemberRepository.findByTeamIdAndMemberId(teamId, memberId)
                .orElseThrow(() -> new EntityNotFoundException("TeamMember not found"));

        TeamRole newRole = teamRoleRepository.findById(newRoleId)
                .orElseThrow(() -> new EntityNotFoundException("New role not found"));

        teamMember.updateRole(newRole);
    }

    private void changeToDefaultRole(Long teamId, Long memberId) {
        TeamMember teamMember = teamMemberRepository.findByTeamIdAndMemberId(teamId, memberId)
                .orElseThrow(() -> new EntityNotFoundException("TeamMember not found"));

        Team team = teamRepository.findById(teamId).orElseThrow(() -> new EntityNotFoundException("team not found"));
        TeamRole defaultRole = teamRoleRepository.findById(team.getBasicRoleId()).orElseThrow(() -> new EntityNotFoundException("role not found"));
        teamMember.updateRole(defaultRole);

        teamMember.updateRole(defaultRole);
    }
    //지정 역할로 변경하는 코드 따로 뺄 것

    //팀 삭제 시 사용되는 것 (기본 역할도 상관 없이 삭제)
    public void deleteRole(Long teamId){
        //teamMember의 role 수정
        List<TeamMember> teamMembers = teamMemberRepository.findAllByTeamId(teamId);
        teamMembers.forEach(TeamMember::reset);
        teamMemberRepository.deleteAll(teamMembers);

        List<TeamRole> roles = teamRoleRepository.findAllByTeamId(teamId);
        roles.forEach(role -> role.setMembers(null));
        teamRoleRepository.deleteAll(roles);
    }

    //팀 정보 페이지에서 - TeamPermission을 가져옴
    public List<TeamRoleDetailResponse> getRolesByTeam(Long teamId){
        List<TeamRoleDetailDto> teamDetailDtos = teamRoleRepository.findTeamRoleDetailsWithMemberCount(teamId);
        return teamDetailDtos.stream().map(this::convertToResponse)
            .toList();
    }

    private TeamRoleDetailResponse convertToResponse(TeamRoleDetailDto dto) {
        Set<TeamPermission> permissions = PermissionUtils.getPermissionsFromBits(
                dto.permissionBits(),
                TeamPermission.class
        );
        return new TeamRoleDetailResponse(
                dto.id(),
                dto.name(),
                permissions,
                dto.memberCount()
        );
    }

    public List<TeamRoleInfoDTO> getRolesInfo(Long teamId){
        return teamRoleRepository.findTeamRoleInfo(teamId);
    }

    public TeamRoleResponse getMembersRole(Long teamId, Member member){
        TeamRole role = teamMemberService.getCurrentUserRole(teamId, member);
        return TeamRoleResponse.from(role);
    }

    private boolean hasTeamPermission(Long teamId, TeamPermission permission) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return teamPermissionEvaluator.hasPermission(auth, teamId, "Team", permission);
    }

    //team 정보 페이지에서 역할 수정 페이지로 넘어갔을 때 사용
    @Transactional(readOnly = true)
    public TeamRoleResponse getRoleDetails(Long teamId, Long roleId) {
        TeamRole role = teamRoleRepository.findWithDetails(teamId, roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));

        return TeamRoleResponse.from(role);
    }

    @Transactional
    public void updateRole(Long teamId, Long roleId, RoleUpdateRequest request){
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found"));
        TeamRole role = teamRoleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));

        String permissionBits = PermissionUtils.createPermissionBits(request.permissions());

        role.update(
                request.roleName(),
                request.description(),
                permissionBits
        );

        teamRoleRepository.save(role);
    }

    //카테고리 정보 수정 시 역할 목록 받아올 때 사용
    public List<CategoryRolePermissionDTO> getRolesWithPermissions(Long teamId, Long categoryId) {
        // 1. 팀의 모든 역할 조회
        List<TeamRole> teamRoles = teamRoleRepository.findAllByTeamId(teamId);

        // 2. 카테고리-역할 권한 조회
        Map<Long, CategoryRolePermission> existingPermissions = categoryPermissionRepository
                .findAllWithRoleByCategoryId(categoryId)
                .stream()
                .collect(Collectors.toMap(
                        crp -> crp.getRole().getId(),
                        Function.identity()
                ));

        // 3. DTO 변환
        return teamRoles.stream()
                .map(role -> {
                    Set<CategoryPermission> permissions = existingPermissions.containsKey(role.getId())
                            ? PermissionUtils.getPermissionsFromBits(
                            existingPermissions.get(role.getId()).getPermissions(),
                            CategoryPermission.class
                    )
                            : Collections.emptySet();

                    return new CategoryRolePermissionDTO(
                            role.getId(),
                            role.getRoleName(),
                            permissions
                    );
                })
                .collect(Collectors.toList());
    }

    public Page<TeamMemberOfRoleDTO> getRoleMembers(Long teamId, Long roleId, int page, int size, String keyword) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return teamMemberRepository.findByRoleId(roleId, keyword, pageRequest)
                .map(TeamMemberOfRoleDTO::from);
    }
}
