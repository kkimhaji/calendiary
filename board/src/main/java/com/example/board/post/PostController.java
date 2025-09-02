package com.example.board.post;

import com.example.board.auth.UserPrincipal;
import com.example.board.image.ImageDomain;
import com.example.board.image.ImageService;
import com.example.board.post.dto.*;
import com.example.board.post.enums.SearchType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/teams/{teamId}")
public class PostController {
    private final PostService postService;
    private final ImageService imageService;
    private final PostSearchService searchService;

    @PostMapping("/category/{categoryId}/posts")
    @PreAuthorize("hasPermission(#categoryId, 'TeamCategory', T(com.example.board.permission.CategoryPermission).CREATE_POST)")
    public ResponseEntity<PostResponse> createPost(@PathVariable(name = "teamId") Long teamId, @PathVariable(name = "categoryId") @P("categoryId") Long categoryId,
                                                   @RequestBody @Valid CreatePostRequest request, @AuthenticationPrincipal UserPrincipal user) throws IOException {
        return ResponseEntity.ok(PostResponse.from(postService.createPost(teamId, categoryId, request, user.getMember())));
    }

    //카테고리의 글 조회
    @GetMapping("/category/{categoryId}/recent")
    @PreAuthorize("hasPermission(#categoryId, 'TeamCategory', T(com.example.board.permission.CategoryPermission).VIEW_POST)")
    public ResponseEntity<CategoryRecentPostsResponse> getPosts(
            @PathVariable(name = "teamId") Long teamId, @PathVariable(name = "categoryId") @P("categoryId") Long categoryId,
            @PageableDefault(size = 20, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(postService.getPostsByCategory(teamId, categoryId, pageable));
    }

    @GetMapping("/category/{categoryId}/posts/{postId}")
    @PreAuthorize("hasPermission(#categoryId, 'TeamCategory', T(com.example.board.permission.CategoryPermission).VIEW_POST)")
    public ResponseEntity<PostDetailDTO> getPost(@PathVariable(name = "postId") Long postId,
                                                 @PathVariable(name = "categoryId") @P("categoryId") Long categoryId, @PathVariable(name = "teamId") Long teamId) {
        return ResponseEntity.ok(postService.getPostDetail(teamId, categoryId, postId));
    }

    @DeleteMapping("/category/{categoryId}/posts/delete/{postId}")
    public void deletePost(@PathVariable(name = "teamId") Long teamId, @PathVariable(name = "categoryId") @P("categoryId") Long categoryId,
                           @PathVariable(name = "postId") Long postId, @AuthenticationPrincipal UserPrincipal user) throws IOException {
        postService.deletePost(teamId, postId, categoryId);
    }

    @PutMapping(path = "/category/{categoryId}/posts/{postId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable("postId") Long postId,
            @PathVariable("teamId") Long teamId,
            @PathVariable("categoryId") Long categoryId,
            @RequestBody @Valid UpdatePostRequestDTO request,
            @AuthenticationPrincipal UserPrincipal user) throws IOException {

        return ResponseEntity.ok(postService.updatePost(categoryId, postId, request));
    }

    //팀의 최근 게시글 목록 조회
    @GetMapping("/recent")
    public ResponseEntity<TeamRecentPostsResponse> getRecentPosts(
            @PathVariable(name = "teamId") Long teamId,
            @PageableDefault(size = 20, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(postService.getRecentPosts(teamId, pageable));
    }

    //이미지 임시 업로드
    @PostMapping("/images/temp-upload")
    public ResponseEntity<String> uploadTempImage(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = imageService.uploadTempImage(file, ImageDomain.POST);
            return ResponseEntity.ok(imageUrl);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/posts/search")
    public ResponseEntity<Page<PostResponse>> searchPosts(
            @PathVariable("teamId") Long teamId,
            @RequestParam("q") String keyword,
            @RequestParam(required = false, name = "categoryId") Long categoryId,
            @PageableDefault(
                    size = 20,
                    sort = "createdDate",
                    direction = Sort.Direction.DESC
            ) Pageable pageable,
            @RequestParam(defaultValue = "BOTH", name = "type") SearchType searchType
    ) {
        return ResponseEntity.ok(
                searchService.searchPosts(teamId, keyword, categoryId, pageable, searchType)
        );
    }
}
