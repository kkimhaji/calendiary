package com.example.board.service;

import com.example.board.config.HtmlSanitizer;
import com.example.board.domain.member.Member;
import com.example.board.domain.post.*;
import com.example.board.domain.team.CategoryRepository;
import com.example.board.domain.team.Team;
import com.example.board.domain.team.TeamCategory;
import com.example.board.domain.team.TeamRepository;
import com.example.board.dto.comment.CommentResponse;
import com.example.board.dto.post.*;
import com.example.board.permission.CategoryPermission;
import com.example.board.permission.TeamPermission;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {
    private final PostRepository postRepository;
    private final TeamRepository teamRepository;
    private final CategoryRepository categoryRepository;
    private final ConcurrentHashMap<Long, AtomicLong> viewCountCache = new ConcurrentHashMap<>();
    private final CommentRepository commentRepository;
    private final ImageService imageService;
    private final HtmlSanitizer htmlSanitizer;
    private final PostImageRepository postImageRepository;
    private final TeamRoleService teamRoleService;

    public Post createPost(Long teamId, Long categoryId, CreatePostRequest request, Member author) throws AccessDeniedException, IOException {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found"));
        TeamCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
        //이미지 처리
        String processedContent = imageService.processContentImages(htmlSanitizer.sanitize(request.content()));

        Post post = request.toEntity(processedContent, team, category, author);

        List<String> permUrls = imageService.extractImageUrlsFromContent(processedContent).stream()
                .filter(url -> url.contains("/perm-images/")).toList();

        for (String permUrl: permUrls){
            String fileName = permUrl.replace("/perm-images/", "");
            PostImage postImage = PostImage.builder()
                    .post(post)
                    .originalFileName(fileName)
                    .storedFileName(fileName)
                    .build();
            post.addImage(postImage);
        }

        return postRepository.save(post);
    }

    //게시글 상세 조회
    @Transactional(readOnly = true)
    public PostDetailDTO getPostDetail(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));
        //조회수 증가: 비동기로 처리
        increaseViewCount(postId);
        List<CommentResponse> comments = commentRepository.findAllByPostIdWithReplies(postId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return PostDetailDTO.from(post, comments);
    }

    private CommentResponse convertToResponse(Comment comment) {
        List<CommentResponse> replies = comment.getReplies().stream()
                .map(this::convertToResponse) // 재귀적으로 대댓글 처리
                .collect(Collectors.toList());

        return CommentResponse.from(comment, replies);
    }

    //최근 게시글 조회
    @Transactional(readOnly = true)
    @Cacheable(value = "recentPosts", key = "#categoryId")
    public List<PostSummaryDTO> getRecentCategoryPosts(Long categoryId, int limit) {
        return postRepository.findRecentPostsByCategoryId(categoryId, PageRequest.of(0, limit));
    }

    public Page<PostListResponse> getRecentPosts(Long teamId, Pageable pageable) {
        return postRepository.findRecentPostsByTeamId(teamId, pageable);
    }

    @Transactional(readOnly = true)
    @Cacheable(key = "{#teamId, #categoryId, #pageable.pageNumber, #pageable.pageSize}") // 캐시 키 설정
    public Page<PostListResponse> getPostsByCategory(Long teamId, Long categoryId,Pageable pageable) {
        return postRepository.findByTeamAndCategory(teamId, categoryId, pageable);
    }

    @Async
    public CompletableFuture<Void> increaseViewCount(Long postId) {
        return CompletableFuture.runAsync(() -> {
                    AtomicLong viewCount = viewCountCache.computeIfAbsent(postId, k -> new AtomicLong(0));
                    viewCount.incrementAndGet();
                }
        );
    }

    // 주기적으로 캐시된 조회수를 DB에 반영
    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void syncViewCountsToDatabase() {
        viewCountCache.forEach((postId, viewCount) -> {
            long count = viewCount.get();
            if (count > 0) {
                postRepository.updateViewCount(postId, count);
                viewCount.addAndGet(-count);
            }
        });
    }

    public void deletePost(Long postId, Long categoryId) throws IOException {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("there is no such post"));

        if (!teamRoleService.hasPermissionOrAuthor(categoryId, postId, CategoryPermission.DELETE_POST))
            throw new AccessDeniedException("게시글을 삭제할 권한이 없습니다.");

        deleteAllPostImages(post);

        postRepository.deleteById(postId);
    }

    public PostResponse updatePost(Long categoryId, Long postId,UpdatePostRequestDTO requestDTO) throws IOException {
        Post post = postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("post not found"));
        TeamCategory category = categoryRepository.findById(categoryId).orElseThrow(() -> new EntityNotFoundException("category not found"));

        //이미지 처리
        String sanitizedContent = htmlSanitizer.sanitize(requestDTO.content());
        String processedContent = imageService.processContentImages(sanitizedContent);


        //기존 이미지 중 삭제 대상 처리
        if (requestDTO.deleteImageIds() != null && !requestDTO.deleteImageIds().isEmpty()){
            deleteImages(post, requestDTO.deleteImageIds());
        }
        Post updatedPost = requestDTO.toEntity(post, category, processedContent);

        List<String> newImageUrls = imageService.extractImageUrlsFromContent(processedContent).stream()
                        .filter(url -> !post.getImages().stream()
                                .anyMatch(img -> url.contains(img.getStoredFileName())))
                        .toList();

        for (String url: newImageUrls){
            String fileName = url.replace("/perm-images/", "");
            PostImage postImage = PostImage.builder()
                    .post(updatedPost).originalFileName(fileName)
                    .storedFileName(fileName).build();
            updatedPost.addImage(postImage);
        }

        return PostResponse.from(postRepository.save(updatedPost));
    }

    private void deleteImages(Post post, List<Long> imageIds) {
        List<PostImage> imagesToDelete = postImageRepository.findAllByIdIn(imageIds);

        for (PostImage image : imagesToDelete) {
            // 이미지가 해당 게시글의 것인지 확인
            if (!image.getPost().getId().equals(post.getId())) {
                throw new IllegalArgumentException("잘못된 이미지 ID입니다.");
            }

            // 파일 시스템에서 파일 삭제
            imageService.deleteImage(image.getStoredFileName());

            // 게시글에서 이미지 제거
            post.removeImage(image);

            // DB에서 이미지 정보 삭제
            postImageRepository.delete(image);
        }
    }
    private void deleteAllPostImages(Post post) throws IOException {
        List<PostImage> images = post.getImages();

        // 4-1. 파일 시스템에서 이미지 삭제
        for (PostImage image : images) {
            imageService.deleteImage(image.getStoredFileName());
        }

        // 4-2. DB에서 이미지 레코드 삭제 (CASCADE 설정 시 자동)
        post.clearImages();
    }

    private void addNewImages(Post post, List<MultipartFile> newImages) throws FileUploadException {
        for (MultipartFile image : newImages) {
            String storedFileName = imageService.saveFile(image);

            PostImage postImage = PostImage.builder()
                    .post(post)
                    .originalFileName(image.getOriginalFilename())
                    .storedFileName(storedFileName)
                    .build();

            post.addImage(postImage);
        }
    }

}
