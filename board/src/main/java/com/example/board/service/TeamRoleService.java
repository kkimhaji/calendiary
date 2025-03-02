package com.example.board.service;

import com.example.board.auth.UserPrincipal;
import com.example.board.domain.member.Member;
import com.example.board.domain.post.Comment;
import com.example.board.domain.post.CommentRepository;
import com.example.board.domain.post.Post;
import com.example.board.domain.post.PostRepository;
import com.example.board.domain.role.CategoryPermissionRepository;
import com.example.board.domain.team.Team;
import com.example.board.domain.team.TeamRepository;
import com.example.board.domain.role.TeamRole;
import com.example.board.domain.role.TeamRoleRepository;
import com.example.board.domain.teamMember.TeamMember;
import com.example.board.domain.teamMember.TeamMemberRepository;
import com.example.board.dto.member.TeamMemberDTO;
import com.example.board.dto.role.*;
import com.example.board.exception.RoleDeletionException;
import com.example.board.permission.*;
import com.example.board.permission.evaluator.CategoryPermissionEvaluator;
import com.example.board.permission.evaluator.TeamPermissionEvaluator;
import com.example.board.permission.utils.PermissionUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamRoleService {
    private final TeamRepository teamRepository;
    private final TeamRoleRepository teamRoleRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final CategoryPermissionRepository permissionRepository;
    private final TeamMemberService teamMemberService;
    private final TeamPermissionEvaluator teamPermissionEvaluator;
    private final CategoryPermissionEvaluator categoryPermissionEvaluator;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public TeamRole createRole(Long teamId, CreateRoleRequest request) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found"));

        // 역할 이름 중복 검사
        if (teamRoleRepository.existsByTeamAndRoleName(team, request.roleName())) {
            throw new RuntimeException("Role name already exists in this team");
        }
        return teamRoleRepository.save(request.toEntity(team));
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
        permissionRepository.deleteAllByRoleId(roleId);
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
    public void deleteRole(Team team){
        //teamMember의 role 수정
        List<TeamMember> teamMembers = teamMemberRepository.findAllByTeam(team);
        teamMembers.forEach(TeamMember::reset);
        teamMemberRepository.deleteAll(teamMembers);

        List<TeamRole> roles = teamRoleRepository.findAllByTeam(team);
        roles.forEach(role -> role.setMembers(null));
        teamRoleRepository.deleteAll(roles);
    }

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

    //team에서 역할 수정
    @Transactional(readOnly = true)
    public RoleDetailsWithMemberListDTO getRoleDetails(Long teamId, Long roleId) {
        TeamRole role = teamRoleRepository.findWithDetails(teamId, roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));
        return new RoleDetailsWithMemberListDTO(
                role.getId(),
                role.getRoleName(),
                role.getDescription(),
                role.getPermissionSet(),
                role.getMembers().stream()
                        .map(tm -> new TeamMemberDTO(
                                tm.getMember().getMemberId(),
                                tm.getTeamNickname()
                        ))
                        .collect(Collectors.toList())
        );
    }
}
