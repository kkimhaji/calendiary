package com.example.board.service;

import com.example.board.auth.UserPrincipal;
import com.example.board.domain.member.Member;
import com.example.board.domain.member.MemberRepository;
import com.example.board.domain.role.TeamRole;
import com.example.board.domain.role.TeamRoleRepository;
import com.example.board.domain.team.Team;
import com.example.board.domain.team.TeamInvite;
import com.example.board.domain.team.TeamInviteRepository;
import com.example.board.domain.team.TeamRepository;
import com.example.board.domain.teamMember.TeamMember;
import com.example.board.domain.teamMember.TeamMemberRepository;
import com.example.board.dto.teamMember.TeamMemberInfo;
import com.example.board.dto.team.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class TeamService {
    private final TeamMemberRepository teamMemberRepository;
    private final TeamRepository teamRepository;
    private final TeamRoleRepository teamRoleRepository;
    private final TeamRoleService teamRoleService;
    private final MemberRepository memberRepository;
    private final CategoryService categoryService;
    private final TeamInviteRepository inviteRepository;

    public TeamMember addMember(AddMemberRequestDTO dto) {

        Team team = teamRepository.findById(dto.teamId())
                .orElseThrow(() -> new EntityNotFoundException("no such team"));
        TeamRole basicRole = teamRoleRepository.findById(team.getBasicRoleId())
                .orElseThrow(() -> new EntityNotFoundException("no basic role"));

        var newMember = memberRepository.findById(dto.memberId())
                .orElseThrow(() -> new UsernameNotFoundException("no such user"));

        TeamMember teamMember = TeamMember.addTeamMember(team, newMember, basicRole);
        return teamMemberRepository.save(teamMember);
    }

    public Team createTeam(Member member, TeamCreateRequestDTO dto) {
        Team newTeam = teamRepository.save(dto.toEntity(member));
        TeamRole admin = teamRoleService.createAdmin(newTeam);

        TeamMember teamMember = TeamMember.createTeam(newTeam, member, admin);
        var basicRole = teamRoleRepository.save(teamRoleService.createBasic(newTeam));
        newTeam.setBasicRoleId(basicRole.getId());
        newTeam.setAdminRoleId(admin.getId());
        teamMemberRepository.save(teamMember);
        return teamRepository.save(newTeam);

//        return TeamCreateResponse.fromEntity(newTeam);
    }

    public TeamInfoPageResponse getTeamInfo(Long teamId, UserPrincipal principal, String inviteCode) {
        TeamInfoDTO teamInfo = teamRepository.findTeamDetailsById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found"));

        // 케이스 1: 로그인 사용자가 팀 멤버인 경우
        if (principal != null) {
            Optional<TeamMemberInfo> memberInfoOpt =
                    teamMemberRepository.findTeamMemberInfoFromTeamIdAndMemberId(
                            teamId, principal.getMember().getMemberId());

            if (memberInfoOpt.isPresent()) {
                return TeamInfoPageResponse.fromTeamMember(teamInfo, memberInfoOpt.get());
            }
        }

        // 케이스 2: 초대 코드가 유효한 경우
        if (inviteCode != null && !inviteCode.isEmpty()) {
            InviteValidationResponse validation = validateInvite(inviteCode);
            if (validation.isValid() && validation.teamId().equals(teamId)) {
                return TeamInfoPageResponse.fromInvite(teamInfo);
            }
        }

        // 케이스 3: 접근 권한 없음
        return TeamInfoPageResponse.noAccess(teamInfo);
    }

    public void deleteTeam(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("team not found"));
        //team에 속한 카테고리 삭제
        categoryService.deleteAllCategoriesInTeam(team);

        //teamMember 수정
        List<TeamMember> teamMembers = teamMemberRepository.findAllByTeamId(teamId);
        teamMembers.forEach(TeamMember::reset);
        teamMemberRepository.deleteAll(teamMembers);

        //team의 role 삭제
        teamRoleService.deleteRole(teamId);

        teamRepository.delete(team);
    }

    public long updateTeamInfo(long teamId, TeamUpdateRequestDTO dto) {
        Team targetTeam = teamRepository.findById(teamId).orElseThrow(() -> new EntityNotFoundException("team not found"));
        if (dto.name() != null && !dto.name().isBlank()) {
            targetTeam.updateName(dto.name());
        }
        if (dto.description() != null) {
            targetTeam.updateDescription(dto.description());
        }
        return targetTeam.getId();
    }

    public InviteResponse createInvite(InviteCreateRequest request) {
        Team team = teamRepository.findById(request.teamId()).orElseThrow(() -> new EntityNotFoundException("team not found"));
        String code = UUID.randomUUID().toString().replace("-", "");
        TeamInvite invite = TeamInvite.builder()
                .code(code)
                .team(team)
                .expiresAt(request.expiresAt())
                .maxUses(request.maxUses())
                .build();

        inviteRepository.save(invite);

        String inviteLink = "http://localhost:3000/teams/" + team.getId() + "/join?code=" + code;
        return new InviteResponse(inviteLink);
    }

    @Transactional(readOnly = true)
    public InviteValidationResponse validateInvite(String code) {
        return inviteRepository.findByCode(code)
                .map(invite -> {
                    boolean isValid = !isExpired(invite) && !isOverused(invite);
                    return new InviteValidationResponse(
                            invite.getTeam().getId(),
                            invite.getTeam().getName(),
                            invite.getTeam().getDescription(),
                            isValid,
                            isValid ? "유효한 초대 코드입니다" : getInvalidReason(invite)
                    );
                })
                .orElse(new InviteValidationResponse(null, null, null, false, "존재하지 않는 코드"));
    }

    @Transactional
    public void joinTeam(Long teamId, TeamJoinRequest request, Member newMember) {
        TeamInvite invite = inviteRepository.findByCode(request.code())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 코드"));

        validateInvite(invite);

        invite.incrementUsedCount();
        inviteRepository.save(invite);

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("no such team"));
        TeamRole basicRole = teamRoleRepository.findById(team.getBasicRoleId())
                .orElseThrow(() -> new EntityNotFoundException("no basic role"));

        TeamMember teamMember = TeamMember.addTeamMember(team, newMember, basicRole);
        teamMemberRepository.save(teamMember);
    }

    // 검증 헬퍼 메서드
    private void validateInvite(TeamInvite invite) {
        if (isExpired(invite)) {
            throw new IllegalArgumentException("만료된 초대 코드");
        }
        if (isOverused(invite)) {
            throw new IllegalArgumentException("사용 횟수 초과");
        }
    }

    private boolean isExpired(TeamInvite invite) {
        return invite.getExpiresAt() != null
                && LocalDateTime.now().isAfter(invite.getExpiresAt());
    }

    private boolean isOverused(TeamInvite invite) {
        return invite.getUsedCount() >= invite.getMaxUses();
    }

    private String getInvalidReason(TeamInvite invite) {
        if (isExpired(invite)) return "만료된 코드입니다";
        if (isOverused(invite)) return "사용 횟수를 초과했습니다";
        return "알 수 없는 오류";
    }
}
