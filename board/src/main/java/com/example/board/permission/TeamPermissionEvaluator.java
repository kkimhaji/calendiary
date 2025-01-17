package com.example.board.permission;

import com.example.board.domain.member.Member;
import com.example.board.domain.role.CategoryPermissionRepository;
import com.example.board.domain.role.CategoryRolePermission;
import com.example.board.domain.team.CategoryRepository;
import com.example.board.domain.team.Team;
import com.example.board.domain.team.TeamCategory;
import com.example.board.domain.team.TeamRepository;
import com.example.board.domain.teamMember.TeamMember;
import com.example.board.domain.teamMember.TeamMemberRepository;
import com.example.board.service.TeamMemberService;
import com.example.board.service.TeamService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.nio.file.attribute.UserPrincipal;

@Component
@RequiredArgsConstructor
@Slf4j
public class TeamPermissionEvaluator implements CustomPermissionEvaluator {

    private final TeamMemberRepository teamMemberRepository;
    private final TeamRepository teamRepository;

//    @Override
//    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
//        if (authentication == null || targetDomainObject == null || !(permission instanceof TeamPermission teamPermission)) {
//            return false;
//        }
//
//        Member member = (Member) authentication.getPrincipal();
//        Long teamId = ((Team) targetDomainObject).getId();
//
//        TeamMember teamMember = teamMemberRepository.findByTeamIdAndMember(teamId, member).orElseThrow(() -> new EntityNotFoundException("Team member not found"));
//        return PermissionUtils.hasPermission(teamMember.getRole().getPermissions(), teamPermission);
//    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (authentication == null || targetId == null || !(permission instanceof TeamPermission)) {
            return false;
        }

        if (!((authentication.getPrincipal()) instanceof UserPrincipal))
            return false;
        try {
            Member member = (Member) authentication.getPrincipal();
            log.info("member: " + member.getMemberId());
            Long teamId = (Long) targetId;
            TeamPermission teamPermission = (TeamPermission) permission;

            TeamMember teamMember = teamMemberRepository.findByTeamIdAndMember(teamId, member)
                    .orElseThrow(() -> new EntityNotFoundException("Team member not found"));
            boolean hasPermission =  PermissionUtils.hasPermission(teamMember.getRole().getPermissions(), teamPermission);

                        // 디버깅을 위한 로그 추가
            log.debug("Permission check - TeamId: {}, Member: {}, Permission: {}, Result: {}",
                    teamId, member.getEmail(), teamPermission, hasPermission);

            return PermissionUtils.hasPermission(teamMember.getRole().getPermissions(), teamPermission);
        } catch (Exception e) {
            log.error("ErrorChecking permission, ", e);
            return false;
        }
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || targetDomainObject == null || !(targetDomainObject instanceof Team)) {
            return false;
        }

        return hasPermission(authentication, ((Team) targetDomainObject).getId(), "Team", permission);
    }

//    @Override
//    public boolean hasPermission(Authentication authentication,
//                                 Object targetDomainObject,
//                                 Object permission) {
//        if (authentication == null || targetDomainObject == null || permission == null) {
//            return false;
//        }
//
//        Member member  = (Member) authentication.getPrincipal();
//        if (targetDomainObject instanceof Team team) {
//            return hasTeamPermission(member, team,
//                    TeamPermission.valueOf(permission.toString()));
//        }
//        return false;
//    }
//
//    @Override
//    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
//        Member member = (Member)authentication.getPrincipal();
//        Long teamId = (Long) targetId;
//
//        // 팀 멤버 역할 확인
//        TeamMember teamMember = teamMemberRepository.findByTeamIdAndMember(teamId, member)
//                .orElse(null);
//
//        if (teamMember == null)
//            return false;
//
//        //역할의 권한 확인
//        return PermissionUtils.hasPermission(teamMember.getRole().getPermissions(), TeamPermission.valueOf(permission.toString()));
//    }
//
//    private boolean hasTeamPermission(Member member, Team team, TeamPermission permission) {
//        return teamMemberRepository.findByTeamAndMember(team, member)
//                .map(user -> PermissionUtils.hasPermission(
//                        user.getRole().getPermissions(),
//                        permission))
//                .orElse(false);
//    }
//
//    public boolean hasPermissionForCategory(Member member, Long categoryId, TeamPermission permission){
//        //카테고리 조회
//        TeamCategory category = categoryRepository.findById(categoryId)
//                .orElseThrow(() -> new EntityNotFoundException("category not found"));
//
//        TeamMember teamMember = teamMemberRepository.findByTeamIdAndMember(category.getTeam().getId(), member)
//                .orElse(null);
//
//        if (teamMember == null)
//            return false;
//
//        //카테고리별 특별 권한 확인
//        CategoryRolePermission categoryPermission = categoryPermissionRepository.findByCategoryAndRole(category, teamMember.getRole())
//                .orElse(null);
//
//        if (categoryPermission != null)
//            return PermissionUtils.hasPermission(categoryPermission.getPermissions(), permission);
//
//        //기본 역할 권한 확인
//        return PermissionUtils.hasPermission(teamMember.getRole().getPermissions(), permission);
//    }

}
