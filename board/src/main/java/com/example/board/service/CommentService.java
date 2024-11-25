package com.example.board.service;

import com.example.board.domain.member.Member;
import com.example.board.domain.post.Comment;
import com.example.board.domain.post.CommentRepository;
import com.example.board.domain.post.Post;
import com.example.board.domain.post.PostRepository;
import com.example.board.domain.role.TeamRole;
import com.example.board.domain.role.TeamRoleRepository;
import com.example.board.domain.team.Team;
import com.example.board.dto.comment.CreateCommentRequest;
import com.example.board.dto.post.PostResponse;
import com.example.board.permission.TeamPermission;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final TeamService teamService;
    private final CategoryService categoryService;
    private final TeamMemberService teamMemberService;

    @Transactional
    public Comment createComment(Member member, Post post, Team team, CreateCommentRequest request) throws AccessDeniedException {
        TeamRole role = teamMemberService.getCurrentUserRole(team.getId(), member);
        if (!categoryService.checkCategoryPermission(post.getCategory().getId(), role.getId(), TeamPermission.CREATE_COMMENT))
            throw new AccessDeniedException("댓글을 작성할 권한이 없습니다.");

        //대댓글인 경우 부모 확인
        Comment parent = null;

        Comment comment = Comment.builder()
                .content(request.content())
                .post(post)
                .author(member)
                .parent(parent)
        .build();

        return commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(Long teamId, Long commentId, Member member){
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(()->new EntityNotFoundException("Comment not found"));

        TeamRole userRole = teamMemberService.getCurrentUserRole(teamId, member);

        if (!comment.getAuthor().equals(member) &&
                !categoryService.checkCategoryPermission(comment.getPost().getCategory().getId(),userRole.getId(), TeamPermission.DELETE_COMMENT)){
            throw new AccessDeniedException("댓글을 삭제할 권한이 없습니다.");
        }

        if (comment.getAuthor().equals(member)){
            comment.deleteByAuthor();
        }

        else
            comment.delete();
    }
}
