package com.example.board.service;

import com.example.board.domain.member.Member;
import com.example.board.domain.member.MemberRepository;
import com.example.board.domain.role.TeamRole;
import com.example.board.domain.role.TeamRoleRepository;
import com.example.board.domain.team.Team;
import com.example.board.domain.team.TeamRepository;
import com.example.board.domain.teamMember.TeamMember;
import com.example.board.domain.teamMember.TeamMemberRepository;
import com.example.board.dto.team.AddMemberRequestDTO;
import com.example.board.dto.team.TeamCreateRequestDTO;
import com.example.board.dto.team.TeamCreateResponse;
import com.example.board.permission.TeamPermission;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

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

    public TeamMember addMember(AddMemberRequestDTO dto){
        Team team = teamRepository.findById(dto.teamId())
                .orElseThrow(()->new EntityNotFoundException("no such team"));
        TeamRole basicRole = teamRoleRepository.findById(team.getBasicRoleId())
                .orElseThrow(()-> new EntityNotFoundException("no basic role"));

        TeamMember teamMember = new TeamMember();
        teamMember.setTeam(team);
        teamMember.setMember(memberRepository.findById(dto.memberId())
                .orElseThrow(() -> new UsernameNotFoundException("no such user")));
        //기본 팀원으로 추가할 땐 basic으로
        teamMember.setRole(basicRole);
        return teamMemberRepository.save(teamMember);
    }


    public TeamCreateResponse createTeam(Member member, TeamCreateRequestDTO dto){
        Team newTeam = teamRepository.save(dto.toEntity(member));
        TeamRole admin = teamRoleService.createAdmin(newTeam);

        TeamMember teamMember = new TeamMember();
        teamMember.createTeam(newTeam, member, admin);
        var basicRole = teamRoleRepository.save(teamRoleService.createBasic(newTeam));
        newTeam.setBasicRoleId(basicRole.getId());
        teamRepository.save(newTeam);
        teamMemberRepository.save(teamMember);

        return TeamCreateResponse.fromEntity(newTeam);
    }

    public void deleteTeam(Long teamId){
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("team not found"));
        //team에 속한 카테고리 삭제
        categoryService.deleteAllCategoriesInTeam(team);

        //teamMember 수정


        //team의 role 삭제
        teamRoleService.deleteRole(team);
    }
}
