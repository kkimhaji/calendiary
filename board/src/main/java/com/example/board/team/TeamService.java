package com.example.board.team;

import com.example.board.auth.UserPrincipal;
import com.example.board.category.CategoryService;
import com.example.board.common.exception.TeamAccessDeniedException;
import com.example.board.common.exception.TeamNotFoundException;
import com.example.board.common.service.EntityValidationService;
import com.example.board.member.Member;
import com.example.board.member.MemberRepository;
import com.example.board.role.TeamRole;
import com.example.board.role.TeamRoleRepository;
import com.example.board.role.TeamRoleService;
import com.example.board.team.dto.*;
import com.example.board.teamInvite.TeamInviteService;
import com.example.board.teamInvite.dto.InviteValidationResponse;
import com.example.board.teamMember.TeamMember;
import com.example.board.teamMember.TeamMemberRepository;
import com.example.board.teamMember.dto.TeamMemberInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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
    private final TeamInviteService inviteService;
    private final EntityValidationService validationService;

    public TeamMember addMember(AddMemberRequestDTO dto) {
        Team team = validationService.validateTeamExists(dto.teamId());
        TeamRole basicRole = validationService.validateRoleExists(team.getBasicRoleId());

        Member newMember = memberRepository.findById(dto.memberId())
                .orElseThrow(() -> new UsernameNotFoundException("no such user"));

        TeamMember teamMember = TeamMember.addTeamMember(team, newMember, basicRole, newMember.getNickname());
        return teamMemberRepository.save(teamMember);
    }

    public Team createTeam(Member member, TeamCreateRequestDTO dto) {
        Team newTeam = teamRepository.save(Team.create(dto.teamName(), dto.description(), member));
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
                .orElseThrow(TeamNotFoundException::new);

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
            InviteValidationResponse validation = inviteService.validateInvite(inviteCode);
            if (validation.isValid() && validation.teamId().equals(teamId)) {
                return TeamInfoPageResponse.fromInvite(teamInfo);
            }
        }

        // 케이스 3: 접근 권한 없음
        throw new TeamAccessDeniedException("해당 팀에 접근할 권한이 없습니다");
    }

    public void deleteTeam(Long teamId) {
        Team team = validationService.validateTeamExists(teamId);
        //team에 속한 카테고리 삭제
        categoryService.deleteAllCategoriesInTeam(team);
        //teamMember 수정
        List<TeamMember> teamMembers = teamMemberRepository.findAllByTeamId(teamId);
        teamMembers.forEach(TeamMember::reset);
        teamMemberRepository.deleteAll(teamMembers);
        //team의 role 삭제
        teamRoleService.deleteRole(teamId);
        inviteService.deleteInvitesByTeamId(teamId);
        teamRepository.delete(team);
    }

    public long updateTeamInfo(long teamId, TeamUpdateRequestDTO dto) {
        Team targetTeam = validationService.validateTeamExists(teamId);

        if (dto.name() != null && !dto.name().isBlank()) {
            targetTeam.updateName(dto.name());
        }
        if (dto.description() != null) {
            targetTeam.updateDescription(dto.description());
        }
        return targetTeam.getId();
    }
}
