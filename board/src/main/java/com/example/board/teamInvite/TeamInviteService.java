package com.example.board.teamInvite;

import com.example.board.common.exception.TeamNicknameDuplicationException;
import com.example.board.member.Member;
import com.example.board.role.TeamRole;
import com.example.board.role.TeamRoleRepository;
import com.example.board.team.Team;
import com.example.board.team.TeamRepository;
import com.example.board.teamInvite.dto.InviteCreateRequest;
import com.example.board.teamInvite.dto.InviteResponse;
import com.example.board.teamInvite.dto.InviteValidationResponse;
import com.example.board.teamInvite.dto.TeamJoinRequest;
import com.example.board.teamMember.TeamMember;
import com.example.board.teamMember.TeamMemberRepository;
import com.example.board.teamMember.TeamMemberService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamInviteService {
    private final TeamRepository teamRepository;
    private final TeamInviteRepository inviteRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamRoleRepository teamRoleRepository;
    private final TeamMemberService teamMemberService;

    @Transactional
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
        if (teamMemberService.isTeamNicknameDuplicate(team, request.teamNickname())) {
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

    @Transactional
    public void deleteInvitesByTeamId(Long teamId){
        inviteRepository.deleteByTeamId(teamId);
    }
}
