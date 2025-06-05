package com.example.board.team;

import com.example.board.auth.UserPrincipal;
import com.example.board.member.Member;
import com.example.board.member.MemberRepository;
import com.example.board.role.TeamRole;
import com.example.board.role.TeamRoleRepository;
import com.example.board.teamMember.TeamMember;
import com.example.board.teamMember.TeamMemberRepository;
import com.example.board.teamMember.dto.TeamMemberInfo;
import com.example.board.common.exception.TeamNicknameDuplicationException;
import com.example.board.category.CategoryService;
import com.example.board.role.TeamRoleService;
import com.example.board.team.dto.*;
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

        Member newMember = memberRepository.findById(dto.memberId())
                .orElseThrow(() -> new UsernameNotFoundException("no such user"));

        TeamMember teamMember = TeamMember.addTeamMember(team, newMember, basicRole, newMember.getNickname());
        return teamMemberRepository.save(teamMember);
    }

    public Team createTeam(Member member, TeamCreateRequestDTO dto) {
        Team newTeam = teamRepository.save(Team.create(dto.teamName(), dto.description(), member, LocalDateTime.now()));
        TeamRole admin = teamRoleService.createAdmin(newTeam);

        TeamMember teamMember = TeamMember.createTeamMember(newTeam, member, admin);
        TeamRole basicRole = teamRoleRepository.save(teamRoleService.createBasic(newTeam));
        newTeam.setBasicRoleId(basicRole.getId());
        newTeam.setAdminRoleId(admin.getId());
        teamMemberRepository.save(teamMember);
        return teamRepository.save(newTeam);
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

    public boolean isTeamNicknameDuplicate(Team team, String teamNickname) {
        if (teamNickname == null || teamNickname.trim().isEmpty()) {
            return false;  // 빈 닉네임은 중복이 아님
        }
        return teamMemberRepository.existsByTeamAndTeamNickname(team, teamNickname.trim());
    }

    // 팀 ID로 중복 검사하는 오버로딩 메서드
    public boolean isTeamNicknameDuplicate(Long teamId, String teamNickname) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found"));
        if (teamNickname == null || teamNickname.trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임을 입력해주세요");
        }
        return isTeamNicknameDuplicate(team, teamNickname);
    }

    public InviteResponse createInvite(InviteCreateRequest request) {
        Team team = teamRepository.findById(request.teamId()).orElseThrow(() -> new EntityNotFoundException("team not found"));
        String code = UUID.randomUUID().toString().replace("-", "");
        TeamInvite invite = TeamInvite.create(code, team, request.expiresAt(), request.maxUses());
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
        request.validate();
        TeamInvite invite = inviteRepository.findByCode(request.code())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 코드"));

        validateInvite(invite);

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("no such team"));

        // 이미 팀 멤버인지 확인
        if (teamMemberRepository.existsByTeamAndMember(team, newMember)) {
            throw new IllegalArgumentException("이미 팀의 멤버입니다");
        }

        // 팀 내 닉네임 중복 검사
        if (isTeamNicknameDuplicate(team, request.teamNickname())) {
            throw new TeamNicknameDuplicationException("이미 사용 중인 팀 닉네임입니다");
        }

        TeamRole basicRole = teamRoleRepository.findById(team.getBasicRoleId())
                .orElseThrow(() -> new EntityNotFoundException("no basic role"));

        TeamMember teamMember = TeamMember.addTeamMember(team, newMember, basicRole, request.teamNickname());
        teamMemberRepository.save(teamMember);
        invite.incrementUsedCount();
        inviteRepository.save(invite);
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
