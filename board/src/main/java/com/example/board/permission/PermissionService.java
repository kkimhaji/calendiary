package com.example.board.permission;

import com.example.board.auth.UserPrincipal;
import com.example.board.member.Member;
import com.example.board.comment.Comment;
import com.example.board.comment.CommentRepository;
import com.example.board.post.Post;
import com.example.board.post.PostRepository;
import com.example.board.role.dto.EditAndDeletePermissionResponse;
import com.example.board.permission.evaluator.DelegatingPermissionEvaluator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.convert.ConversionService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {
    private final DelegatingPermissionEvaluator permissionEvaluator;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ConversionService conversionService;

    //단일 권한 검사
    @Cacheable(value = "permission-checks", key = "#targetId + '-' + #permission.getCode()")
    public boolean checkPermission(Long targetId, PermissionType permission) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String targetType = getTargetType(permission);
        return permissionEvaluator.hasPermission(auth, targetId, targetType, permission);
    }

    private PermissionType apply(String p) {
        return conversionService.convert(p, PermissionType.class);
    }

    // 다중 권한 검사 최적화
    public Map<String, Boolean> checkMultiplePermission(Long targetId, List<String> permissions) {
        Map<String, Boolean> results = new HashMap<>();
        // 한 번에 권한 객체 생성
        Map<String, PermissionType> permissionObjects = permissions.stream()
                .filter(p -> {
                    try {
                        apply(p);
                        return true;
                    } catch (IllegalArgumentException e) {
                        results.put(p, false);
                        return false;
                    }
                })
                .collect(Collectors.toMap(
                        p -> p,
                        this::apply
                ));
        // 권한 검사 수행
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String targetType = null;

        for (Map.Entry<String, PermissionType> entry : permissionObjects.entrySet()) {
            String permissionName = entry.getKey();
            PermissionType permission = entry.getValue();

            if (targetType == null) {
                targetType = getTargetType(permission);
            }
            results.put(permissionName, permissionEvaluator.hasPermission(
                    auth, targetId, targetType, permission));
        }
        return results;
    }

    // 게시글 권한 검사
    @Cacheable(value = "edit-delete-permissions", key = "'post-' + #postId")
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
    @Cacheable(value = "edit-delete-permissions", key = "'comment-' + #commentId")
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

    private Member getLoginMember() {
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
