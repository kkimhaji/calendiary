package com.example.board.teamMember;

import com.example.board.comment.Comment;
import com.example.board.comment.CommentRepository;
import com.example.board.common.exception.TeamMemberNotFoundException;
import com.example.board.common.service.EntityValidationService;
import com.example.board.image.ImageService;
import com.example.board.member.Member;
import com.example.board.member.dto.AddTeamMemberToRoleDTO;
import com.example.board.permission.PermissionService;
import com.example.board.post.Post;
import com.example.board.post.PostRepository;
import com.example.board.role.TeamRole;
import com.example.board.team.Team;
import com.example.board.team.dto.TeamInfoResponse;
import com.example.board.team.dto.TeamListDTO;
import com.example.board.teamMember.dto.MemberProfileResponse;
import com.example.board.teamMember.dto.RemoveMemberRequestDTO;
import com.example.board.teamMember.dto.TeamMemberDTO;
import com.example.board.teamMember.dto.TeamMemberInfoListDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.board.permission.TeamPermission.MANAGE_MEMBERS;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamMemberService {
    private final TeamMemberRepository teamMemberRepository;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final ImageService imageService;
    private final EntityValidationService validationService;
    private final PermissionService permissionService;

    @Transactional(readOnly = true)
    public TeamRole getCurrentUserRole(Long teamId, Member member) {
        validationService.validateTeamExists(teamId);
        return teamMemberRepository.findByTeamIdAndMember(teamId, member)
                .map(TeamMember::getRole)
                .orElseThrow(() -> new TeamMemberNotFoundException("You are not a member of this team!!"));
    }

    @Transactional
    public String updateTeamNickname(Long teamId, Member member, String newNickname) {
        validationService.validateTeamExists(teamId);
        validationService.validateMemberExists(member.getMemberId());
        validateTeamNickname(newNickname);
        TeamMember teamMember = teamMemberRepository.findByTeamIdAndMemberId(teamId, member.getMemberId())
                .orElseThrow(() -> new TeamMemberNotFoundException("cannot find team member"));
        String normalizedNickname = newNickname.trim();

        teamMember.updateTeamNickname(normalizedNickname);
        return normalizedNickname;
    }

    public List<TeamListDTO> getTeams(Member member) {
        validationService.validateMemberExists(member.getMemberId());
        Long memberId = member.getMemberId();
        return teamMemberRepository.findTeamListByMemberId(memberId);
    }

    public List<TeamMemberDTO> getMembersByRole(Long roleId) {
        validationService.validateRoleExists(roleId);
        return teamMemberRepository.findMembersByRoleId(roleId);
    }

    public MemberProfileResponse getTeamMemberProfile(Long teamMemberId) {
        MemberProfileResponse dto = teamMemberRepository.findMemberProfileByTeamMemberId(teamMemberId)
                .orElseThrow(() -> new TeamMemberNotFoundException("team member not found"));

        return new MemberProfileResponse(
                maskEmail(dto.email()),
                dto.teamNickname(),
                dto.roleName(),
                dto.joinedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<TeamMemberInfoListDTO> getTeamMembersWithRole(Long teamId) {
        validationService.validateTeamExists(teamId);
        List<TeamMemberInfoListDTO> members = teamMemberRepository.findMembersByTeamId(teamId);
        return members.stream()
                .map(dto -> new TeamMemberInfoListDTO(
                        dto.teamMemberId(),
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
            maskedLocalPart = localPart.charAt(0) + "*".repeat(localPart.length() - 1);
        } else {
            // 2글자 초과인 경우 첫 2글자만 보이고 나머지 *
            maskedLocalPart = localPart.substring(0, 3) + "*".repeat(localPart.length() - 3);
        }

        return maskedLocalPart + "@" + domainPart;
    }

    @Transactional(readOnly = true)
    public Page<AddTeamMemberToRoleDTO> getTeamMembers(
            Long teamId,
            int page,
            int size,
            String keyword
    ) {
        validationService.validateTeamExists(teamId);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("joinedAt").descending());
        return teamMemberRepository.findAllWithDetailsByTeamId(
                teamId,
                "%" + keyword + "%",
                pageRequest
        ).map(AddTeamMemberToRoleDTO::new);
    }

    @Transactional(readOnly = true)
    public List<TeamInfoResponse> getTeamInfoWithTeamNickname(Long memberId) {
        validationService.validateMemberExists(memberId);
        return teamMemberRepository.findTeamInfoAndNicknameAndRoleByMemberId(memberId);
    }

    @Transactional
    public void leaveTeam(Long teamId, Member member, boolean deleteContents) {
        Team team = validationService.validateTeamExists(teamId);
        validationService.validateMemberExists(member.getMemberId());

        TeamMember teamMember = teamMemberRepository.findByTeamIdAndMember(teamId, member)
                .orElseThrow(() -> new TeamMemberNotFoundException("member is not in team"));

        Long adminRoleId = team.getAdminRoleId();
        if (teamMember.getRole().getId().equals(adminRoleId) && isLastOwner(teamId, adminRoleId)) {
            throw new IllegalStateException("팀의 관리자는 탈퇴할 수 없습니다. 다른 사용자에게 관리자 권한을 부여한 후 탈퇴해주세요.");
        }

        if (deleteContents) {
            deleteTeamMemberContents(teamId, member.getMemberId());
        } else {
            anonymizeMemberContent(teamMember);
        }
        teamMemberRepository.delete(teamMember);
    }

    @Transactional
    public void removeMember(Long teamId, Long teamMemberId, RemoveMemberRequestDTO request) {
        Team team = validationService.validateTeamExists(teamId);

        if (!permissionService.checkPermission(teamId, MANAGE_MEMBERS))
            throw new AccessDeniedException("팀 멤버 관리 권한이 없습니다.");

        TeamMember teamMember = validationService.validateTeamMemberExists(teamMemberId);

        // 4. 같은 팀인지 확인
        if (!teamMember.getTeam().getId().equals(teamId)) {
            throw new IllegalArgumentException("해당 팀의 멤버가 아닙니다.");
        }
        // 5. 팀 소유자는 탈퇴 불가
        // 6. 자기 자신은 탈퇴 불가 (일반 탈퇴 사용)
        /** 나중에 검증 코드 추가할 것 **/

        if (request.deleteContent()) {
            // 옵션 1: 모든 게시글과 댓글 삭제
            deleteTeamMemberContents(teamId, teamMemberId);
            log.info("팀 멤버 ID: {}의 모든 게시글과 댓글을 삭제했습니다.", teamMemberId);
        } else {
            // 옵션 2: 작성자만 null로 변경 (익명 처리)
            anonymizeMemberContent(teamMember);
            log.info("팀 멤버 ID: {}의 게시글과 댓글을 익명 처리했습니다.", teamMemberId);
        }


        teamMemberRepository.delete(teamMember);
    }

    private boolean isLastOwner(Long teamId, Long roleId) {
        validationService.validateTeamExists(teamId);
        validationService.validateRoleExists(roleId);
        return teamMemberRepository.countByTeamIdAndRoleId(teamId, roleId) <= 1;
    }

    @Transactional
    private void deleteTeamMemberContents(Long teamId, Long memberId) {
        validationService.validateTeamExists(teamId);
        validationService.validateMemberExists(memberId);

        teamMemberRepository.findByTeamIdAndMemberId(teamId, memberId)
                .orElseThrow(() -> new TeamMemberNotFoundException("member is not in team"));

        // 사용자가 작성한 댓글 삭제
        // 1. depth가 깊은 댓글부터 삭제 (최대 depth부터 0까지)
        int maxDepth = commentRepository.findMaxDepthByTeamIdAndMemberId(teamId, memberId)
                .orElse(0);

        for (int depth = maxDepth; depth >= 0; depth--) {
            commentRepository.deleteAllByTeamIdAndMemberIdAndDepth(teamId, memberId, depth);
        }

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
                // 이미지 삭제 실패해도 게시글은 삭제 진행
                commentRepository.deleteAllByPostId(post.getId());
                postRepository.delete(post);
            }
        }
    }

    private void anonymizeMemberContent(TeamMember teamMember) {
        // 1. 게시글 작성자를 null로 변경
        List<Post> posts = postRepository.findAllByTeamMember(teamMember);
        posts.forEach(post -> post.setTeamMember(null));
        postRepository.saveAll(posts);
        log.debug("익명 처리된 게시글 수: {}", posts.size());

        // 2. 댓글 작성자를 null로 변경
        List<Comment> comments = commentRepository.findAllByTeamMember(teamMember);
        comments.forEach(comment -> comment.setTeamMember(null));
        commentRepository.saveAll(comments);
        log.debug("익명 처리된 댓글 수: {}", comments.size());
    }

    // 팀 ID로 중복 검사하는 오버로딩 메서드
    public boolean isTeamNicknameDuplicate(Long teamId, String teamNickname) {
        Team team = validationService.validateTeamExists(teamId);

        if (teamNickname == null || teamNickname.trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임을 입력해주세요");
        }
        return isTeamNicknameDuplicate(team, teamNickname);
    }

    public boolean isTeamNicknameDuplicate(Team team, String teamNickname) {
        if (teamNickname == null || teamNickname.trim().isEmpty()) {
            return false;  // 빈 닉네임은 중복이 아님
        }
        return teamMemberRepository.existsByTeamAndTeamNickname(team, teamNickname.trim());
    }

    private void validateTeamNickname(String nickname) {
        if (nickname == null) {
            throw new IllegalArgumentException("닉네임은 필수입니다.");
        }

        if (nickname.trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임은 공백일 수 없습니다.");
        }

        if (nickname.trim().length() > 20) { // 최대 길이 제한 (선택사항)
            throw new IllegalArgumentException("닉네임은 20자를 초과할 수 없습니다.");
        }
    }

}
