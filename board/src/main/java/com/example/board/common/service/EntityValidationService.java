package com.example.board.common.service;

import com.example.board.category.CategoryRepository;
import com.example.board.category.TeamCategory;
import com.example.board.comment.Comment;
import com.example.board.comment.CommentRepository;
import com.example.board.common.exception.*;
import com.example.board.diary.Diary;
import com.example.board.diary.DiaryRepository;
import com.example.board.member.Member;
import com.example.board.member.MemberRepository;
import com.example.board.post.Post;
import com.example.board.post.PostRepository;
import com.example.board.role.TeamRole;
import com.example.board.role.TeamRoleRepository;
import com.example.board.team.Team;
import com.example.board.team.TeamRepository;
import com.example.board.teamMember.TeamMember;
import com.example.board.teamMember.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EntityValidationService {
    private final TeamRepository teamRepository;
    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;
    private final TeamRoleRepository teamRoleRepository;
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final DiaryRepository diaryRepository;
    private final TeamMemberRepository teamMemberRepository;

    public Team validateTeamExists(Long teamId) {
        if (teamId == null) throw new IllegalArgumentException("teamId는 null일 수 없습니다.");

        return teamRepository.findById(teamId)
                .orElseThrow(TeamNotFoundException::new);
    }

    public TeamCategory validateCategoryExists(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(CategoryNotFoundException::new);
    }

    public Post validatePostExists(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(PostNotFoundException::new);
    }

    public TeamRole validateRoleExists(Long roleId) {
        return teamRoleRepository.findById(roleId)
                .orElseThrow(RoleNotFoundException::new);
    }

    public Comment validateCommentExists(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(CommentNotFoundException::new);
    }

    public Comment validateCommentExists(Long commentId, String message) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(message));
    }

    public Member validateMemberExists(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
    }

    public Diary validateDiaryExists(Long diaryId){
        return diaryRepository.findById(diaryId)
                .orElseThrow(DiaryNotFoundException::new);
    }

    public void validatePostPath(Long teamId, Long categoryId, Long postId) {
        Post post = validatePostExists(postId);
        validateCategoryExists(categoryId);
        validateTeamExists(teamId);

        if (!post.getCategory().getId().equals(categoryId)) {
            throw new CategoryNotFoundException();
        }

        if (!post.getCategory().getTeam().getId().equals(teamId)) {
            throw new TeamNotFoundException();
        }
    }

    public void validatePath(Long teamId, Long categoryId){
        validateCategoryExists(categoryId);
        validateTeamExists(teamId);
    }

    public TeamMember validateTeamMemberExists(Long teamMemberId){
        return teamMemberRepository.findById(teamMemberId)
                .orElseThrow(TeamMemberNotFoundException::new);
    }
}
