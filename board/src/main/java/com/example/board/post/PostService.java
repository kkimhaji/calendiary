package com.example.board.post;

import com.example.board.auth.UserPrincipal;
import com.example.board.category.TeamCategory;
import com.example.board.comment.CommentRepository;
import com.example.board.common.exception.PostNotFoundException;
import com.example.board.common.exception.TeamMemberNotFoundException;
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

import static com.example.board.permission.CategoryPermission.CREATE_POST;
import static com.example.board.permission.CategoryPermission.VIEW_POST;

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
    public Post createPost(Long teamId, Long categoryId, CreatePostRequest req, Member author) throws IOException {
        Team team = validationService.validateTeamExists(teamId);
        TeamCategory category = validationService.validateCategoryExists(categoryId);

        if (!permissionService.checkPermission(categoryId, CREATE_POST))
            throw new AccessDeniedException("게시글을 삭제할 권한이 없습니다.");

        // HTML 콘텐츠 처리 (임시 → 영구 이미지 변환)
        String sanitized = htmlSanitizer.sanitize(req.content());
        String processedContent = imageService.processContentImages(sanitized, ImageDomain.POST);

        TeamMember teamMember = teamMemberRepository
                .findByTeamIdAndMember(teamId, author)
                .orElseThrow(() -> new TeamMemberNotFoundException("team member not found"));

        Post post = Post.create(req.title(), processedContent, author, category, team, teamMember);

        // 새로운 이미지 등록
        registerContentImages(post, processedContent);

        return postRepository.save(post);
    }

    @Transactional
    public PostResponse updatePost(Long categoryId, Long postId, UpdatePostRequestDTO request, Member member) throws IOException {

        Post post = validationService.validatePostExists(postId);
        TeamCategory category = validationService.validateCategoryExists(categoryId);

        if (!post.getAuthor().equals(member)) {
            throw new AccessDeniedException("작성자 본인만 수정할 수 있습니다.");
        }
        // HTML 콘텐츠 처리
        String sanitized = htmlSanitizer.sanitize(request.content());
        String processedContent = imageService.processContentImages(sanitized, ImageDomain.POST);

        // 기존 이미지 삭제 처리
        if (!request.deleteImageIds().isEmpty()) {
            deleteImages(post, request.deleteImageIds());
        }

        // 게시글 업데이트
        post.update(request.title(), processedContent, category);

        // 새로운 이미지 등록 (증분 방식)
        registerNewContentImages(post, processedContent);

        return PostResponse.from(postRepository.save(post));
    }

    //게시글 상세 조회
    public PostDetailDTO getPostDetail(Long teamId, Long categoryId, Long postId) {
        validationService.validatePath(teamId, categoryId);
        Post post = validationService.validatePostExists(postId);
        permissionService.checkPermission(categoryId, VIEW_POST);
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

    @Transactional
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
    @Transactional
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
        Post post = postRepository.findByIdWithImages(postId)
                .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다."));

        if (!permissionService.hasPermissionOrAuthor(categoryId, postId, CategoryPermission.DELETE_POST))
            throw new AccessDeniedException("게시글을 삭제할 권한이 없습니다.");

        commentRepository.deleteAllByPostId(postId);
        imageService.deleteAllPostImages(post);

        postRepository.deleteById(postId);
    }

    // 새로운 이미지 처리 로직을 별도 메서드로 분리
//    private void processNewImages(Post post, String processedContent) {
//        List<String> newUrls = imageService.extractImageUrlsFromContent(processedContent).stream()
//                .filter(u -> u.startsWith(ImageDomain.POST.permPrefix()))
//                .filter(u -> post.getImages().stream()
//                        .noneMatch(img -> u.endsWith(img.getStoredFileName())))
//                .toList();
//
//        for (String url : newUrls) {
//            String fileName = url.substring(url.lastIndexOf('/') + 1);   // ← 변경
//            post.addImage(PostImage.createPostImage(post, fileName, fileName));
//        }
//    }

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

//    @Transactional
//    private void addNewImages(Post post, List<MultipartFile> newImages) throws FileUploadException {
//        for (MultipartFile image : newImages) {
//            String storedFileName = imageService.saveFile(image, ImageDomain.POST);
//            PostImage postImage = PostImage.createPostImage(post, image.getOriginalFilename(), storedFileName);
//            post.addImage(postImage);
//        }
//    }

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

    private void registerContentImages(Post post, String processedContent) {
        List<String> permUrls = imageService.extractImageUrlsFromContent(processedContent).stream()
                .filter(u -> u.startsWith(ImageDomain.POST.permPrefix()))
                .toList();

        for (String url : permUrls) {
            String fileName = url.substring(url.lastIndexOf('/') + 1);
            post.addImage(PostImage.createPostImage(post, fileName, fileName));
        }
    }

    private void registerNewContentImages(Post post, String processedContent) {
        List<String> newUrls = imageService.extractImageUrlsFromContent(processedContent).stream()
                .filter(u -> u.startsWith(ImageDomain.POST.permPrefix()))
                .filter(u -> post.getImages().stream()
                        .noneMatch(img -> u.endsWith(img.getStoredFileName())))
                .toList();

        for (String url : newUrls) {
            String fileName = url.substring(url.lastIndexOf('/') + 1);
            post.addImage(PostImage.createPostImage(post, fileName, fileName));
        }
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