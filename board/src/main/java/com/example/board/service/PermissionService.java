package com.example.board.service;

import com.example.board.auth.UserPrincipal;
import com.example.board.domain.member.Member;
import com.example.board.domain.post.Comment;
import com.example.board.domain.post.CommentRepository;
import com.example.board.domain.post.Post;
import com.example.board.domain.post.PostRepository;
import com.example.board.dto.role.EditAndDeletePermissionResponse;
import com.example.board.permission.CategoryPermission;
import com.example.board.permission.PermissionType;
import com.example.board.permission.TeamPermission;
import com.example.board.permission.evaluator.DelegatingPermissionEvaluator;
import com.example.board.permission.utils.StringToPermissionConverter;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class PermissionService {
    private final DelegatingPermissionEvaluator permissionEvaluator;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ConversionService conversionService;

    //단일 권한 검사
    public boolean checkPermission(Long targetId, PermissionType permission) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String targetType = getTargetType(permission);
        return permissionEvaluator.hasPermission(auth, targetId, targetType, permission);
    }

    public Map<String, Boolean> checkMultiplePermission(Long targetId, List<String> permissions){
        Map<String, Boolean> results = new HashMap<>();

        for (String permission : permissions) {
            try {
                //문자열 -> PermissionType 변환
                PermissionType permissionType = conversionService.convert(permission, PermissionType.class);
                results.put(permission, checkPermission(targetId, permissionType));
            } catch (IllegalArgumentException e){
                results.put(permission, false);
            }
        }
        return results;
    }
    // 게시글 권한 검사
    public EditAndDeletePermissionResponse checkEditAndDeletePostPermission(Long postId) {
        return checkEditAndDeletePermission(
                () -> postRepository.findById(postId)
                        .orElseThrow(() -> new EntityNotFoundException("Post not found")),
                Post::getAuthor,
                post -> post.getCategory().getId(),
                CategoryPermission.DELETE_POST // ✅ PermissionType으로 전달
        );
    }

    // 댓글 권한 검사
    public EditAndDeletePermissionResponse checkEditAndDeleteCommentPermission(Long commentId) {
        return checkEditAndDeletePermission(
                () -> commentRepository.findById(commentId)
                        .orElseThrow(() -> new EntityNotFoundException("Comment not found")),
                Comment::getAuthor,
                comment -> comment.getPost().getCategory().getId(),
                CategoryPermission.DELETE_COMMENT // ✅ PermissionType으로 전달
        );
    }

    public boolean hasPermissionOrAuthor(Long categoryId, Long postId, CategoryPermission permission) {
        Member author = postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("post not found")).getAuthor();
        return checkPermission(categoryId, permission) || author.getMemberId().equals(getLoginMember().getMemberId());
    }

    private Member getLoginMember(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ((UserPrincipal) auth.getPrincipal()).getMember();
    }

    private String getTargetType(PermissionType permission) {
        if (permission instanceof TeamPermission) return "Team";
        if (permission instanceof CategoryPermission) return "TeamCategory";
        throw new IllegalArgumentException("Unsupported permission type");
    }

    private <T> EditAndDeletePermissionResponse checkEditAndDeletePermission(
            Supplier<T> entitySupplier,
            Function<T, Member> authorExtractor,
            Function<T, Long> categoryIdExtractor,
            PermissionType requiredPermission
    ) {
        try {
            Member loginMember = getLoginMember();
            T entity = entitySupplier.get();
            Member author = authorExtractor.apply(entity);

            // 1. 작성자 본인인 경우
            if (loginMember.getMemberId().equals(author.getMemberId())) {
                return EditAndDeletePermissionResponse.of(true, true);
            }

            // 2. 권한 검사: 기존 `hasCategoryPermission` → `checkPermission` 호출
            Long categoryId = categoryIdExtractor.apply(entity);
            boolean canDelete = checkPermission(categoryId, requiredPermission);
            return EditAndDeletePermissionResponse.of(false, canDelete);

        } catch (EntityNotFoundException e) {
            return EditAndDeletePermissionResponse.of(false, false);
        }
    }


}
