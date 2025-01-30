package com.example.board.service;

import com.example.board.config.HtmlSanitizer;
import com.example.board.domain.member.Member;
import com.example.board.domain.post.*;
import com.example.board.domain.team.CategoryRepository;
import com.example.board.domain.team.Team;
import com.example.board.domain.team.TeamCategory;
import com.example.board.domain.team.TeamRepository;
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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

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

    public Post createPost(Long teamId, Long categoryId, CreatePostRequest request, Member author) throws AccessDeniedException, FileUploadException {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found"));
        TeamCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        Post post = request.toEntity(htmlSanitizer.sanitize(request.content()), team, category, author);
        if (request.images()!= null && !request.images().isEmpty()){
            for (MultipartFile image : request.images()) {
                String storedFileName = imageService.saveFile(image);

                PostImage postImage = PostImage.builder()
                        .post(post)
                        .originalFileName(image.getOriginalFilename())
                        .storedFileName(storedFileName)
                        .build();
                post.addImage(postImage);
            }
        }
        return postRepository.save(post);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "teamPosts", key = "#teamId + '_' + #categoryId")
    public Page<PostListResponse> getPostsByCategory(Long teamId, Long categoryId,Pageable pageable) {

        return postRepository.findByTeamAndCategory(teamId, categoryId, pageable);
    }

    //게시글 상세 조회
    @Transactional(readOnly = true)
    public PostDetailDTO getPostDetail(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));
        //조회수 증가: 비동기로 처리
        increaseViewCount(postId);

        List<Comment> comments = commentRepository.findAllByPostIdWithReplies(postId);

        return PostDetailDTO.from(post, comments);
    }

    //최근 게시글 조회
    @Transactional(readOnly = true)
    @Cacheable(value = "recentPosts", key = "#categoryId")
    public List<PostSummaryDTO> getRecentCategoryPosts(Long categoryId, int limit) {
        return postRepository.findRecentPostsByCategoryId(categoryId, PageRequest.of(0, limit));
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

    public void deletePost(Long postId, Long categoryId){
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("there is no such post"));

        if (!teamRoleService.hasPermissionOrAuthor(categoryId, postId, CategoryPermission.DELETE_POST))
            throw new AccessDeniedException("게시글을 삭제할 권한이 없습니다.");
        postRepository.deleteById(postId);
    }

    public PostResponse updatePost(Long categoryId, Long postId,UpdatePostRequestDTO requestDTO) throws FileUploadException {
        Post post = postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("post not found"));

        if (!teamRoleService.hasPermissionOrAuthor(categoryId, postId, CategoryPermission.EDIT_POST)){
            throw new AccessDeniedException("게시글을 수정할 권한이 없습니다.");
        }

        String sanitizedContent = htmlSanitizer.sanitize(requestDTO.content());
        post.update(requestDTO.title(), sanitizedContent);

        if (requestDTO.deleteImageIds() != null && !requestDTO.deleteImageIds().isEmpty())
            deleteImages(post, requestDTO.deleteImageIds());

        if (requestDTO.images() != null && !requestDTO.images().isEmpty())
            addNewImages(post, requestDTO.images());

        return PostResponse.from(post);
    }

    private void deleteImages(Post post, List<Long> imageIds) {
        List<PostImage> imagesToDelete = postImageRepository.findAllByIdIn(imageIds);

        for (PostImage image : imagesToDelete) {
            // 이미지가 해당 게시글의 것인지 확인
            if (!image.getPost().equals(post)) {
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

    public Page<PostListResponse> getRecentPosts(Long teamId, Pageable pageable){
        return postRepository.findRecentPostsByTeamId(teamId, pageable);
    }

}
