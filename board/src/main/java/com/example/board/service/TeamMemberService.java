package com.example.board.service;

import com.example.board.domain.member.Member;
import com.example.board.domain.comment.CommentRepository;
import com.example.board.domain.post.Post;
import com.example.board.domain.post.PostRepository;
import com.example.board.domain.role.TeamRole;
import com.example.board.domain.team.Team;
import com.example.board.domain.team.TeamRepository;
import com.example.board.domain.teamMember.TeamMember;
import com.example.board.domain.teamMember.TeamMemberRepository;
import com.example.board.dto.member.AddTeamMemberToRoleDTO;
import com.example.board.dto.teamMember.MemberProfileResponse;
import com.example.board.dto.teamMember.TeamMemberDTO;
import com.example.board.dto.teamMember.TeamMemberInfoListDTO;
import com.example.board.dto.team.TeamInfoResponse;
import com.example.board.dto.team.TeamListDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamMemberService {
    private final TeamMemberRepository teamMemberRepository;
    private final TeamRepository teamRepository;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final ImageService imageService;

    @Transactional(readOnly = true)
    public TeamRole getCurrentUserRole(Long teamId, Member member) {
        return teamMemberRepository.findByTeamIdAndMember(teamId, member)
                .map(TeamMember::getRole)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this team!!"));
    }

    @Transactional
    public String updateTeamNickname(Long teamId, Member member, String newNickname) {
        TeamMember teamMember = teamMemberRepository.findByTeamIdAndMemberId(teamId, member.getMemberId())
                .orElseThrow(() -> new EntityNotFoundException("cannot find team member"));

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

    public MemberProfileResponse getTeamMemberProfile(Long teamMemberId){
        MemberProfileResponse dto = teamMemberRepository.findMemberProfileByTeamMemberId(teamMemberId)
                .orElseThrow(() -> new EntityNotFoundException("team member not found"));

        return new MemberProfileResponse(
                maskEmail(dto.email()),
                dto.teamNickname(),
                dto.roleName(),
                dto.joinedAt()
                );
    }

    @Transactional(readOnly = true)
    public List<TeamMemberInfoListDTO> getTeamMembersWithRole(Long teamId) {
        List<TeamMemberInfoListDTO> members = teamMemberRepository.findMembersByTeamId(teamId);
        return members.stream()
                .map(dto -> new TeamMemberInfoListDTO(
                        maskEmail(dto.email()),
                        dto.teamNickname(),
                        dto.roleName(),
                        dto.roleId()
                ))
                .collect(Collectors.toList());
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }

        String[] parts = email.split("@", 2);
        String localPart = parts[0];
        String domainPart = parts[1];

        // 로컬 부분 마스킹 처리
        String maskedLocalPart;
        if (localPart.length() <= 2) {
            // 2글자 이하인 경우 첫 글자만 보이고 나머지 *
            maskedLocalPart = localPart.substring(0, 1) + "*".repeat(localPart.length() - 1);
        } else {
            // 2글자 초과인 경우 첫 2글자만 보이고 나머지 *
            maskedLocalPart = localPart.substring(0, 3) + "*".repeat(localPart.length() - 3);
        }

        return maskedLocalPart + "@" + domainPart;
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
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("joinedAt").descending());
        return teamMemberRepository.findAllWithDetailsByTeamId(
                teamId,
                "%" + keyword + "%",
                pageRequest
        ).map(AddTeamMemberToRoleDTO::new);
    }

    @Transactional(readOnly = true)
    public List<TeamInfoResponse> getTeamInfoWithTeamNickname(Long memberId) {
        return teamMemberRepository.findTeamInfoAndNicknameByMemberId(memberId);
    }

    public void leaveTeam(Long teamId, Member member, boolean deleteContents) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("team not found"));

        TeamMember teamMember = teamMemberRepository.findByTeamIdAndMember(teamId, member)
                .orElseThrow(() -> new EntityNotFoundException("member is not in team"));

        Long adminRoleId  = team.getAdminRoleId();
        if (teamMember.getRole().getId().equals(adminRoleId) && isLastOwner(teamId, adminRoleId)) {
            throw new IllegalStateException("팀의 관리자는 탈퇴할 수 없습니다. 다른 사용자에게 관리자 권한을 부여한 후 탈퇴해주세요.");
        }

        if (deleteContents) {
            deleteTeamMemberContents(teamId, member.getMemberId());
        }

        teamMemberRepository.delete(teamMember);
    }

    private boolean isLastOwner(Long teamId, Long roleId) {
        return teamMemberRepository.countByTeamIdAndRoleId(teamId, roleId) <= 1;
    }
    private void deleteTeamMemberContents(Long teamId, Long memberId) {
        // 1. 사용자가 작성한 댓글 삭제
        commentRepository.deleteAllByTeamIdAndMemberId(teamId, memberId);

        // 2. 사용자가 작성한 게시글 삭제
        List<Post> posts = postRepository.findAllByTeamIdAndAuthorId(teamId, memberId);
        for (Post post : posts) {
            try {
                // 게시글 이미지 파일 삭제
                imageService.deleteAllPostImages(post);

                // 게시글에 달린 모든 댓글 삭제
                commentRepository.deleteAllByPostId(post.getId());

                // 게시글 삭제
                postRepository.delete(post);
            } catch (IOException e) {
//                log.error("이미지 삭제 중 오류 발생: " + e.getMessage());
                // 이미지 삭제 실패해도 게시글은 삭제 진행
                commentRepository.deleteAllByPostId(post.getId());
                postRepository.delete(post);
            }
        }
    }
}
