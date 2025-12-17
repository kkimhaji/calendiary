package com.example.board.permission.evaluator;

import com.example.board.auth.UserPrincipal;
import com.example.board.member.Member;
import com.example.board.role.TeamRole;
import com.example.board.team.Team;
import com.example.board.teamMember.TeamMember;
import com.example.board.teamMember.TeamMemberRepository;
import com.example.board.permission.TeamPermission;
import com.example.board.permission.utils.PermissionConverter;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class TeamPermissionEvaluator implements CustomPermissionEvaluator {

    private final TeamMemberRepository teamMemberRepository;

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (authentication == null || targetId == null || !(permission instanceof TeamPermission)) {
            return false;
        }

        if (!((authentication.getPrincipal()) instanceof UserPrincipal))
            return false;
        try {
            Member member = ((UserPrincipal) authentication.getPrincipal()).getMember();
            Long teamId = (Long) targetId;
            TeamPermission teamPermission = (TeamPermission) permission;

            // 팀 멤버 존재 여부 확인 (예외 발생 방지)
            Optional<TeamMember> teamMemberOpt = teamMemberRepository.findByTeamIdAndMember(teamId, member);
            if (teamMemberOpt.isEmpty()) {
                log.warn("팀 멤버를 찾을 수 없습니다: teamId={}, memberId={}", teamId, member.getMemberId());
                return false; // 예외 대신 false 반환으로 403 처리
            }

            TeamMember teamMember = teamMemberOpt.get();
            TeamRole role = teamMember.getRole();
            byte[] permissionBytes = role.getPermissionBytes();

            boolean hasPermission = PermissionConverter.hasPermissionOptimized(permissionBytes, teamPermission);

            log.debug("팀 권한 검사 결과: teamId={}, permission={}, member={}, result={}",
                    teamId, teamPermission, member.getMemberId(), hasPermission);

            return hasPermission;

        } catch (EntityNotFoundException e) {
            // EntityNotFoundException을 명시적으로 처리
            log.warn("엔티티를 찾을 수 없습니다 - teamId: {}, message: {}", targetId, e.getMessage());
            return false;
        } catch (Exception e) {
            // 기타 모든 예외를 안전하게 처리
            log.error("팀 권한 검사 중 예상치 못한 오류 발생 - teamId: {}, error: {}", targetId, e.getMessage(), e);
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
}
