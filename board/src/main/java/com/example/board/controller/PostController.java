package com.example.board.controller;

import com.example.board.auth.UserPrincipal;
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
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/teams/{teamId}")
public class PostController {

    private final PostService postService;
    private final ImageService imageService;

    @PostMapping("/category/{categoryId}/posts")
    @PreAuthorize("hasPermission(#categoryId, 'TeamCategory', T(com.example.board.permission.CategoryPermission).CREATE_POST)")
    public ResponseEntity<PostResponse> createPost(@PathVariable(name="teamId") Long teamId, @PathVariable(name="categoryId") @P("categoryId") Long categoryId, @RequestBody CreatePostRequest request, @AuthenticationPrincipal UserPrincipal user) throws FileUploadException {
        Post post = postService.createPost(teamId, categoryId, request, user.getMember());
        return ResponseEntity.ok(PostResponse.from(post));
    }

    //카테고리의 글 조회
    @GetMapping("/category/{categoryId}/recent")
    @PreAuthorize("hasPermission(#categoryId, 'TeamCategory', T(com.example.board.permission.CategoryPermission).VIEW_POST)")
    public ResponseEntity<Page<PostListResponse>> getPosts(
            @PathVariable(name="teamId") Long teamId, @PathVariable(name="categoryId") @P("categoryId") Long categoryId,
            @AuthenticationPrincipal UserPrincipal user,
            @PageableDefault(size = 20, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PostListResponse> posts = postService.getPostsByCategory(teamId, categoryId, user.getMember(), pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/category/{categoryId}/posts/{postId}")
    @PreAuthorize("hasPermission(#categoryId, 'TeamCategory', T(com.example.board.permission.CategoryPermission).VIEW_POST)")
    public ResponseEntity<PostDetailDTO> getPost(@PathVariable(name="postId") @P("categoryId") Long postId) {
        return ResponseEntity.ok(postService.getPostDetail(postId));
    }

    @PostMapping("/category/{categoryId}/posts/delete/{postId}")
    @PreAuthorize("hasPermission(#categoryId, 'TeamCategory', T(com.example.board.permission.CategoryPermission).DELETE_POST)")
    public void deletePost(@PathVariable(name="teamId") Long teamId, @PathVariable(name="categoryId") @P("categoryId") Long categoryId, @PathVariable(name="postId") Long postId, @AuthenticationPrincipal UserPrincipal user) {
        postService.deletePost(postId, user.getMember(), categoryId, teamId);
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
    public ResponseEntity<PostResponse> updatePost(@PathVariable(name="postId") Long postId, @PathVariable(name="teamId") Long teamId, @PathVariable(name="categoryId") Long categoryId, @RequestBody UpdatePostRequestDTO request, @AuthenticationPrincipal UserPrincipal user) throws FileUploadException {
        return ResponseEntity.ok(postService.updatePost(teamId, categoryId, postId, user.getMember(), request));
    }

    //팀의 최근 게시글 목록 조회
    @GetMapping("/recent")
    public ResponseEntity<Page<PostListResponse>> getRecentPosts(
            @PathVariable(name="teamId") Long teamId,
            @PageableDefault(size = 20, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable){
        return ResponseEntity.ok(postService.getRecentPosts(teamId, pageable));
    }
}
