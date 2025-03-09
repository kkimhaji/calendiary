package com.example.board.service;

import com.example.board.domain.member.Member;
import com.example.board.domain.role.TeamRole;
import com.example.board.domain.teamMember.TeamMember;
import com.example.board.domain.teamMember.TeamMemberRepository;
import com.example.board.dto.member.AddTeamMemberToRoleDTO;
import com.example.board.dto.member.TeamMemberDTO;
import com.example.board.dto.member.TeamMemberInfoListDTO;
import com.example.board.dto.team.TeamListDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamMemberService {
    private final TeamMemberRepository teamMemberRepository;

    @Transactional(readOnly = true)
    public TeamRole getCurrentUserRole(Long teamId, Member member) {
        return teamMemberRepository.findByTeamIdAndMember(teamId, member)
                .map(TeamMember::getRole)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this team!!"));
    }

    @Transactional
    public String updateTeamNickname(Long teamMemberId, String newNickname) {
        TeamMember teamMember = teamMemberRepository.findById(teamMemberId)
                .orElseThrow(() -> new EntityNotFoundException("team member not found"));

        teamMember.updateTeamNickname(newNickname);
        return newNickname;
    }

    public List<TeamListDTO> getTeams(Member member) {
        Long memberId = member.getMemberId();
        return teamMemberRepository.findTeamListByMemberId(memberId);
    }

    public List<TeamMemberDTO> getMembersByRole(Long roleId) {
        return teamMemberRepository.findMembersByRoleId(roleId);
    }

    @Transactional(readOnly = true)
    public List<TeamMemberInfoListDTO> getTeamMembersWithRole(Long teamId) {
        return teamMemberRepository.findMembersByTeamId(teamId);
    }

    @Transactional(readOnly = true)
    public List<AddTeamMemberToRoleDTO> getTeamMembers(Long teamId) {
        return teamMemberRepository.findAllWithDetailsByTeamId(teamId)
                .stream()
                .map(AddTeamMemberToRoleDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<AddTeamMemberToRoleDTO> getTeamMembers(
            Long teamId,
            int page,
            int size,
            String keyword
    ) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return teamMemberRepository.findAllWithDetailsByTeamId(
                teamId,
                "%" + keyword + "%",
                pageRequest
        ).map(AddTeamMemberToRoleDTO::new);
    }
}
