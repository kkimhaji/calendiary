package com.example.board.post;

import com.example.board.auth.UserPrincipal;
import com.example.board.category.TeamCategory;
import com.example.board.comment.CommentRepository;
import com.example.board.common.service.EntityValidationService;
import com.example.board.config.HtmlSanitizer;
import com.example.board.image.ImageDomain;
import com.example.board.image.ImageService;
import com.example.board.member.Member;
import com.example.board.permission.CategoryPermission;
import com.example.board.permission.PermissionService;
import com.example.board.post.dto.*;
import com.example.board.team.Team;
import com.example.board.teamMember.TeamMember;
import com.example.board.teamMember.TeamMemberRepository;
import com.google.common.annotations.VisibleForTesting;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {
    private final PostRepository postRepository;
    private final ConcurrentHashMap<Long, AtomicLong> viewCountCache = new ConcurrentHashMap<>();
    private final ImageService imageService;
    private final HtmlSanitizer htmlSanitizer;
    private final PostImageRepository postImageRepository;
    private final PermissionService permissionService;
    private final TeamMemberRepository teamMemberRepository;
    private final CommentRepository commentRepository;
    private final EntityValidationService validationService;

    @Transactional
    public Post createPost(Long teamId, Long categoryId, CreatePostRequest request, Member author) throws AccessDeniedException, IOException {
        Team team = validationService.validateTeamExists(teamId);
        TeamCategory category = validationService.validateCategoryExists(categoryId);
        //이미지 처리
        String processedContent = imageService.processContentImages(htmlSanitizer.sanitize(request.content()));

        TeamMember teamMember = teamMemberRepository.findByTeamIdAndMember(teamId, author)
                .orElseThrow(() -> new EntityNotFoundException("team member not found"));

        Post post = Post.create(request.title(), request.content(), author, category, team, teamMember);

        List<String> permUrls = imageService.extractImageUrlsFromContent(processedContent).stream()
                .filter(url -> url.contains("/perm-images/")).toList();

        for (String permUrl : permUrls) {
            String fileName = permUrl.replace("/perm-images/", "");
            PostImage postImage = PostImage.createPostImage(post, fileName, fileName);
            post.addImage(postImage);
        }

        return postRepository.save(post);
    }

    //게시글 상세 조회
    public PostDetailDTO getPostDetail(Long teamId, Long categoryId, Long postId) {
        validationService.validatePath(teamId, categoryId);
        Post post = validationService.validatePostExists(postId);
        //조회수 증가: 비동기로 처리
        increaseViewCount(postId);

        return PostDetailDTO.from(post);
    }

    public TeamRecentPostsResponse getRecentPosts(Long teamId, Pageable pageable) {
        String teamName = validationService.validateTeamExists(teamId).getName();
        Page<PostListResponse> posts = postRepository.findRecentPostsByTeamId(teamId, pageable);

        return new TeamRecentPostsResponse(teamName, posts);
    }

    @Cacheable(key = "{#teamId, #categoryId, #pageable.pageNumber, #pageable.pageSize}") // 캐시 키 설정
    public CategoryRecentPostsResponse getPostsByCategory(Long teamId, Long categoryId, Pageable pageable) {
        validationService.validateTeamExists(teamId);
        String categoryName = validationService.validateCategoryExists(categoryId).getName();
        Page<PostListResponse> posts = postRepository.findByTeamAndCategory(teamId, categoryId, pageable);
        return new CategoryRecentPostsResponse(categoryName, posts);
    }

    public CompletableFuture<Void> increaseViewCount(Long postId) {
        return CompletableFuture.runAsync(() -> {
            processViewCountIncrease(postId);
        });
    }

    // 동기식 핵심 로직 - 테스트 가능
    @VisibleForTesting
    public void processViewCountIncrease(Long postId) {
        AtomicLong viewCount = viewCountCache.computeIfAbsent(postId, k -> new AtomicLong(0));
        viewCount.incrementAndGet();

        if (viewCount.get() >= 10) {
            syncSinglePostViewCount(postId);
        }
    }
//    @Async
//    @Transactional
//    public CompletableFuture<Void> increaseViewCount(Long postId) {
//        return CompletableFuture.runAsync(() -> {
//            AtomicLong viewCount = viewCountCache.computeIfAbsent(postId, k -> new AtomicLong(0));
//            viewCount.incrementAndGet();
//
//            if (viewCount.get() >= 10) {
//                syncSinglePostViewCount(postId);
//            }
//        });
//    }

    public void syncSinglePostViewCount(Long postId) {
        AtomicLong countAtomic = viewCountCache.get(postId);
        if (countAtomic != null) {
            long count = countAtomic.getAndSet(0);
            if (count > 0) {
                postRepository.updateViewCount(postId, count);
            }
        }
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

    @Transactional
    public void deletePost(Long teamId, Long postId, Long categoryId) throws IOException {
        validationService.validatePath(teamId, categoryId);
        Post post = validationService.validatePostExists(postId);

        if (!permissionService.hasPermissionOrAuthor(categoryId, postId, CategoryPermission.DELETE_POST))
            throw new AccessDeniedException("게시글을 삭제할 권한이 없습니다.");

        commentRepository.deleteAllByPostId(postId);
        imageService.deleteAllPostImages(post);

        postRepository.deleteById(postId);
    }

    @Transactional
    public PostResponse updatePost(Long categoryId, Long postId, UpdatePostRequestDTO requestDTO) throws IOException {
        Post post = validationService.validatePostExists(postId);
        TeamCategory category = validationService.validateCategoryExists(categoryId);

        //이미지 처리
        String sanitizedContent = htmlSanitizer.sanitize(requestDTO.content());
        String processedContent = imageService.processContentImages(sanitizedContent);

        //기존 이미지 중 삭제 대상 처리
        if (requestDTO.deleteImageIds() != null && !requestDTO.deleteImageIds().isEmpty()) {
            deleteImages(post, requestDTO.deleteImageIds());
        }
        post.update(requestDTO.title(), processedContent, category);
        // 새로운 이미지 처리
        processNewImages(post, processedContent);

        return PostResponse.from(postRepository.save(post));
    }

    // 새로운 이미지 처리 로직을 별도 메서드로 분리
    private void processNewImages(Post post, String processedContent) {
        List<String> newImageUrls = imageService.extractImageUrlsFromContent(processedContent).stream()
                .filter(url -> !post.getImages().stream()
                        .anyMatch(img -> url.contains(img.getStoredFileName())))
                .toList();

        for (String url : newImageUrls) {
            String fileName = url.replace("/perm-images/", "");
            PostImage postImage = PostImage.createPostImage(post, fileName, fileName);
            post.addImage(postImage);
        }
    }

    @Transactional
    private void deleteImages(Post post, List<Long> imageIds) {
        List<PostImage> imagesToDelete = postImageRepository.findAllByIdIn(imageIds);

        for (PostImage image : imagesToDelete) {
            // 이미지가 해당 게시글의 것인지 확인
            if (!image.getPost().getId().equals(post.getId())) {
                throw new IllegalArgumentException("잘못된 이미지 ID입니다.");
            }
            // 파일 시스템에서 파일 삭제
            imageService.deleteImage(image.getStoredFileName(), ImageDomain.POST);
            // 게시글에서 이미지 제거
            post.removeImage(image);
            // DB에서 이미지 정보 삭제
            postImageRepository.delete(image);
        }
    }

    @Transactional
    private void addNewImages(Post post, List<MultipartFile> newImages) throws FileUploadException {
        for (MultipartFile image : newImages) {
            String storedFileName = imageService.saveFile(image, ImageDomain.POST);
            PostImage postImage = PostImage.createPostImage(post, image.getOriginalFilename(), storedFileName);
            post.addImage(postImage);
        }
    }

    public Page<PostListResponse> getLatestPostsByUserTeams(UserPrincipal user, Pageable pageable) {
        // 1. 사용자가 속한 팀 ID 목록 조회
        List<Long> teamIds = teamMemberRepository.findTeamIdsByMemberId(user.getMember().getMemberId());

        // 2. 팀 ID 목록으로 게시글 조회
        return postRepository.findByTeamIds(teamIds, pageable)
                .map(PostListResponse::from);
    }

    public Page<PostListResponse> findPostsByTeamAndMember(Long authorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        return postRepository.findPostListResponseByTeamMemberId(authorId, pageable);
    }

    /**
     * 테스트용: 특정 게시글의 캐시된 조회수 조회
     *
     * @param postId 게시글 ID
     * @return 캐시된 조회수 (캐시에 없으면 0 반환)
     */
    @VisibleForTesting
    public long getCachedViewCount(Long postId) {
        AtomicLong count = viewCountCache.get(postId);
        return count != null ? count.get() : 0;
    }

    @VisibleForTesting
    public void clearViewCountCache() {
        viewCountCache.clear();
    }

    /**
     * 테스트용: 특정 게시글의 캐시된 조회수 설정
     */
    @VisibleForTesting
    public void setCachedViewCount(Long postId, long count) {
        viewCountCache.put(postId, new AtomicLong(count));
    }

    /**
     * 테스트용: 현재 캐시 크기 반환
     */
    @VisibleForTesting
    public int getCacheSize() {
        return viewCountCache.size();
    }
}
