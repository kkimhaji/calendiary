package com.example.board.service;

import com.example.board.domain.member.Member;
import com.example.board.domain.post.Comment;
import com.example.board.domain.post.CommentRepository;
import com.example.board.domain.post.Post;
import com.example.board.domain.post.PostRepository;
import com.example.board.domain.role.TeamRole;
import com.example.board.domain.role.TeamRoleRepository;
import com.example.board.domain.team.Team;
import com.example.board.dto.comment.CommentResponse;
import com.example.board.dto.comment.CreateCommentRequest;
import com.example.board.dto.post.PostResponse;
import com.example.board.permission.CategoryPermission;
import com.example.board.permission.TeamPermission;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final TeamService teamService;
    private final CategoryService categoryService;
    private final TeamMemberService teamMemberService;
    private static final int MAX_DEPTH = 2;

    @Transactional
    public CommentResponse createComment(Member member, Long postId, CreateCommentRequest request) throws AccessDeniedException {
        Post post = postRepository.findById(postId).orElseThrow(()->new EntityNotFoundException("post not found"));
        if (!categoryService.checkCategoryPermission(post.getCategory().getId(), member, CategoryPermission.CREATE_COMMENT))
            throw new AccessDeniedException("댓글을 작성할 권한이 없습니다.");

        System.out.println("member id: " + member.getMemberId() + ", nickname: " + member.getNickname());
        //부모가 있을 때만 부모 댓글 조회
        Comment parent = request.parentCommentId().map(id -> commentRepository.findById(id))
                .orElseThrow(() -> new EntityNotFoundException("Parent comment not found"))
                .orElse(null);

        int depth = parent != null ? parent.getDepth() + 1 : 0;
        if (depth>MAX_DEPTH) throw new IllegalArgumentException("최대 답글 깊이를 초과했습니다.");

        Comment comment = request.toEntity(post, member, parent);
        post.addComment(comment);

        return CommentResponse.from(commentRepository.save(comment));
    }

    @Transactional
    public void deleteComment(Long teamId, Long commentId, Member member){
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(()->new EntityNotFoundException("Comment not found"));

        if (!comment.getAuthor().equals(member) &&
                !categoryService.checkCategoryPermission(comment.getPost().getCategory().getId(), member, CategoryPermission.DELETE_COMMENT)){
            throw new AccessDeniedException("댓글을 삭제할 권한이 없습니다.");
        }

        if (comment.getAuthor().equals(member)){
            comment.deleteByAuthor();
        }

        else
            comment.delete();
    }

}
