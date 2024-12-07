package com.example.board.service;

import com.example.board.domain.member.Member;
import com.example.board.domain.role.TeamRole;
import com.example.board.domain.teamMember.TeamMember;
import com.example.board.domain.teamMember.TeamMemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeamMemberService {
    private final TeamMemberRepository teamMemberRepository;

    @Transactional(readOnly = true)
    public TeamRole getCurrentUserRole(Long teamId, Member member){
        return teamMemberRepository.findByTeamIdAndMember(teamId, member)
                .map(TeamMember::getRole)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this team!!"));
    }

    @Transactional
    public String updateTeamNickname(Long teamMemberId, String newNickname){
        TeamMember teamMember = teamMemberRepository.findById(teamMemberId)
                .orElseThrow(() -> new EntityNotFoundException("team member not found"));

        teamMember.updateTeamNickname(newNickname);
        return newNickname;
    }

}
