package com.example.board.member;

import com.example.board.auth.token.RefreshToken;
import com.example.board.auth.token.RefreshTokenRepository;
import com.example.board.auth.token.Token;
import com.example.board.auth.token.TokenRepository;
import com.example.board.comment.CommentRepository;
import com.example.board.common.service.EntityValidationService;
import com.example.board.diary.DiaryService;
import com.example.board.image.ImageService;
import com.example.board.post.Post;
import com.example.board.post.PostRepository;
import com.example.board.role.TeamRole;
import com.example.board.team.Team;
import com.example.board.team.TeamService;
import com.example.board.teamMember.TeamMember;
import com.example.board.teamMember.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MemberDeletionService {
    private final MemberRepository memberRepository;
    private final DiaryService diaryService;
    private final TeamMemberRepository teamMemberRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ImageService imageService;
    private final EntityValidationService validationService;
    private final TeamService teamService;
    private final TokenRepository tokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * 회원 탈퇴 - 모든 관련 데이터 삭제
     */
    public void deleteMember(Member member, String password, PasswordEncoder passwordEncoder) {
        // 1. 비밀번호 확인
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        Long memberId = member.getMemberId();
        log.info("회원 탈퇴 시작 - memberId: {}", memberId);

        try {
            deleteAllMemberTokens(memberId);

            // 2. 일기 삭제
            diaryService.deleteAllMemberDiaries(memberId);

            // 3. 팀 멤버십 처리 (팀 삭제 포함)
            deleteAllMemberTeamMemberships(memberId);

            // 4. 회원 정보 삭제
            memberRepository.delete(member);

            log.info("회원 탈퇴 완료 - memberId: {}", memberId);
        } catch (Exception e) {
            log.error("회원 탈퇴 중 오류 발생 - memberId: {}, error: {}", memberId, e.getMessage(), e);
            throw new RuntimeException("회원 탈퇴 처리 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 회원의 모든 팀 멤버십 및 팀 삭제
     */
    private void deleteAllMemberTeamMemberships(Long memberId) {
        log.info("회원 팀 멤버십 전체 삭제 시작 - memberId: {}", memberId);

        List<TeamMember> teamMemberships = teamMemberRepository.findAllByMemberId(memberId);
        int deletedMembershipCount = teamMemberships.size();
        int deletedTeamCount = 0;

        for (TeamMember teamMember : teamMemberships) {
            Team team = teamMember.getTeam();
            Long teamId = team.getId();

            // 현재 팀의 멤버 수 확인
            long memberCount = teamMemberRepository.countByTeamId(teamId);

            log.debug("팀 멤버 수 확인 - teamId: {}, memberCount: {}", teamId, memberCount);

            // 1. 회원의 팀 콘텐츠 삭제 (게시글, 댓글)
            deleteTeamMemberContents(teamId, memberId);

            // 2. 양방향 관계 동기화
            TeamRole role = teamMember.getRole();
            if (role != null) {
                role.getMembers().remove(teamMember);
            }
            team.getMembers().remove(teamMember);

            // 3. TeamMember 삭제
            teamMemberRepository.delete(teamMember);

            // 4. 마지막 멤버였다면 팀 전체 삭제
            if (memberCount == 1) {
                log.info("마지막 멤버 탈퇴로 팀 삭제 시작 - teamId: {}, teamName: {}",
                        teamId, team.getName());

                try {
                    teamService.deleteTeamWithoutMembers(teamId);
                    deletedTeamCount++;
                    log.info("팀 삭제 완료 - teamId: {}", teamId);
                } catch (Exception e) {
                    log.error("팀 삭제 실패 - teamId: {}, error: {}", teamId, e.getMessage(), e);
                }
            }
        }

        log.info("회원 팀 멤버십 전체 삭제 완료 - memberId: {}, 삭제된 멤버십 수: {}, 삭제된 팀 수: {}",
                memberId, deletedMembershipCount, deletedTeamCount);
    }

    /**
     * 특정 팀에서 회원의 콘텐츠 삭제 (게시글, 댓글)
     */
    private void deleteTeamMemberContents(Long teamId, Long memberId) {
        validationService.validateTeamExists(teamId);
        validationService.validateMemberExists(memberId);

        // 1. 댓글 삭제
        commentRepository.deleteAllByTeamIdAndMemberId(teamId, memberId);

        // 2. 게시글 삭제
        List<Post> posts = postRepository.findAllByTeamIdAndAuthorId(teamId, memberId);

        for (Post post : posts) {
            try {
                imageService.deleteAllPostImages(post);
                commentRepository.deleteAllByPostId(post.getId());
                postRepository.delete(post);
            } catch (IOException e) {
                log.error("게시글 이미지 삭제 실패 - postId: {}, error: {}", post.getId(), e.getMessage());
                commentRepository.deleteAllByPostId(post.getId());
                postRepository.delete(post);
            }
        }

        log.info("팀 게시글 삭제 완료 - teamId: {}, memberId: {}, 삭제된 게시글 수: {}",
                teamId, memberId, posts.size());
    }

    /**
     * 회원의 모든 토큰 삭제 (Access Token + Refresh Token)
     */
    private void deleteAllMemberTokens(Long memberId) {
        log.info("회원 토큰 전체 삭제 시작 - memberId: {}", memberId);

        int deletedAccessTokenCount = 0;
        int deletedRefreshTokenCount = 0;

        try {
            // 1. Access Token 삭제
            List<Token> accessTokens = tokenRepository.findAllByMemberId(memberId);
            deletedAccessTokenCount = accessTokens.size();
            tokenRepository.deleteAllByMemberId(memberId);

            log.info("Access Token 삭제 완료 - memberId: {}, 삭제된 토큰 수: {}",
                    memberId, deletedAccessTokenCount);
        } catch (Exception e) {
            log.error("Access Token 삭제 실패 - memberId: {}, error: {}", memberId, e.getMessage());
        }

        try {
            // 2. Refresh Token 삭제
            List<RefreshToken> refreshTokens = refreshTokenRepository.findAllByMemberId(memberId);
            deletedRefreshTokenCount = refreshTokens.size();
            refreshTokenRepository.deleteAllByMemberId(memberId);

            log.info("Refresh Token 삭제 완료 - memberId: {}, 삭제된 토큰 수: {}",
                    memberId, deletedRefreshTokenCount);
        } catch (Exception e) {
            log.error("Refresh Token 삭제 실패 - memberId: {}, error: {}", memberId, e.getMessage());
        }

        log.info("회원 토큰 전체 삭제 완료 - memberId: {}, Access Token: {}, Refresh Token: {}",
                memberId, deletedAccessTokenCount, deletedRefreshTokenCount);
    }

}
