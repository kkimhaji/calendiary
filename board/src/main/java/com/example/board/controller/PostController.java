package com.example.board.controller;

import com.example.board.domain.member.Member;
import com.example.board.domain.post.Post;
import com.example.board.dto.post.*;
import com.example.board.service.ImageService;
import com.example.board.service.PostService;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/teams/{teamId}")
public class PostController {

    private final PostService postService;
    private final ImageService imageService;

    @PostMapping("/category/{categoryId}/posts")
    @PreAuthorize("@teamPermissionEvaluator.hasPermissionForCategory(principal, #categoryId, 'CREATE_POST')")
    public ResponseEntity<PostResponse> createPost(@PathVariable Long teamId, @PathVariable Long categoryId, @RequestBody CreatePostRequest request, @AuthenticationPrincipal Member member) throws FileUploadException {
        Post post = postService.createPost(teamId, categoryId, request, member);
        return ResponseEntity.ok(PostResponse.from(post));
    }

    //카테고리의 글 조회
    @GetMapping("/category/{categoryId}/posts")
    @PreAuthorize("@teamPermissionEvaluator.hasPermissionForCategory(principal, #categoryId, 'VIEW_POST')")
    public ResponseEntity<Page<PostListResponse>> getPosts(
            @PathVariable Long teamId, @PathVariable Long categoryId,
            @AuthenticationPrincipal Member member,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PostListResponse> posts = postService.getPostsByCategory(teamId, categoryId, member, pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/category/{categoryId}/posts/{postId}")
    @PreAuthorize("@teamPermissionEvaluator.hasPermissionForCategory(principal, #categoryId, 'VIEW_POST')")
    public ResponseEntity<PostDetailDTO> getPost(@PathVariable Long postId) {
        return ResponseEntity.ok(postService.getPostDetail(postId));
    }

    @PostMapping("/category/{categoryId}/posts/delete/{postId}")
    @PreAuthorize("@teamPermissionEvaluator.hasPermissionForCategory(principal, #categoryId, 'DELETE_POST')")
    public void deletePost(@PathVariable Long teamId, @PathVariable Long categoryId, @PathVariable Long postId, @AuthenticationPrincipal Member member) {
        postService.deletePost(postId, member, categoryId, teamId);
    }

    @PostMapping("/category/{categoryId}/posts/{postId}/images")
    // CK Editor는 'upload'로 파일 전송
    public ResponseEntity<ImageResponse> uploadImages(@RequestParam("upload") MultipartFile file) {
        try {
            return ResponseEntity.ok(imageService.savedImages(file));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ImageResponse(e.getMessage())
            );
        }
    }

    @PutMapping("/category/{categoryId}/posts/{postId}")
    public ResponseEntity<PostResponse> updatePost(@PathVariable Long postId, @PathVariable Long teamId, @PathVariable Long categoryId, @RequestBody UpdatePostRequestDTO request, @AuthenticationPrincipal Member member) throws FileUploadException {
        return ResponseEntity.ok(postService.updatePost(teamId, categoryId, postId, member, request));
    }
}
