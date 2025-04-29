package com.example.board.service;

import com.example.board.domain.member.Member;
import com.example.board.domain.post.Comment;
import com.example.board.domain.post.CommentRepository;
import com.example.board.domain.post.Post;
import com.example.board.domain.post.PostRepository;
import com.example.board.dto.comment.CommentResponse;
import com.example.board.dto.comment.CreateCommentRequest;
import com.example.board.permission.CategoryPermission;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

        Comment parent = null;
        //부모가 있을 때만 부모 댓글 조회
        if (request.parentCommentId()!=null) {
            Long parentId = request.parentCommentId();

            parent = commentRepository.findById(parentId)
                    .orElseThrow(() -> new EntityNotFoundException("Parent comment not found"));
        }

        Comment comment = request.toEntity(post, member, parent);
        post.addComment(comment);

        return CommentResponse.from(commentRepository.save(comment));
    }

    @Transactional
    public void deleteComment(Long commentId, Member member){
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

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsInPost(Long postId){
        postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("post not found"));
        return commentRepository.findByPostIdAndParentIsNull(postId).stream()
                .map(CommentResponse::from).toList();
    }

    public Page<CommentResponse> findCommentsByTeamAndMember(Long teamId, Long memberId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return commentRepository.findByTeamIdAndAuthorId(teamId, memberId, pageable)
                .map(CommentResponse::from);
    }

}
