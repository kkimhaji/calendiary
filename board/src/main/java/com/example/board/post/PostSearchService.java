package com.example.board.post;

import com.example.board.common.service.EntityValidationService;
import com.example.board.config.AsyncConfig;
import com.example.board.post.enums.SearchType;
import com.example.board.post.dto.PostResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PostSearchService {
    private final PostRepository postRepository;
    private final AsyncConfig asyncConfig;
    @Qualifier("asyncTaskExecutor")
    private final Executor asyncExecutor;
    private final EntityValidationService validationService;

    public Page<PostResponse> searchPosts(
            Long teamId,
            String keyword,
            Long categoryId,
            Pageable pageable,
            SearchType searchType
    ) {

        validationService.validateTeamExists(teamId);
        validationService.validateCategoryExists(categoryId);

        if (searchType == null) {
            searchType = SearchType.BOTH;
        }

        if (keyword == null || keyword.trim().isEmpty()) {
            return Page.empty(pageable);
        }
        switch (searchType) {
            case TITLE:
                // 제목만 검색
                return postRepository.searchByTitle(keyword, teamId, categoryId, pageable)
                        .map(PostResponse::from);
            case CONTENT:
                // 내용만 검색
                return postRepository.searchByContent(keyword, teamId, categoryId, pageable)
                        .map(PostResponse::from);
            case BOTH:
            default:
                // 제목 또는 내용 검색 (단일 쿼리 사용 권장)
                return postRepository.searchByTitleOrContent(keyword, teamId, categoryId, pageable)
                        .map(PostResponse::from);
        }
    }

    private Page<PostResponse> processResults(
            CompletableFuture<Page<Post>> titleFuture,
            CompletableFuture<Page<Post>> contentFuture,
            Pageable pageable
    ) {
        return CompletableFuture.allOf(titleFuture, contentFuture)
                .thenApplyAsync(v -> {
                    try {
                        List<Post> combined = Stream.concat(
                                titleFuture.get().getContent().stream(),
                                contentFuture.get().getContent().stream()
                        ).distinct().collect(Collectors.toList());

                        List<Post> sorted = sortPosts(combined, pageable.getSort());
                        return paginateList(sorted, pageable);

                    } catch (Exception e) {
                        throw new RuntimeException("검색 처리 실패", e);
                    }
                }, asyncExecutor).join();
    }

    // 리스트 정렬 메서드
    private List<Post> sortPosts(List<Post> posts, Sort sort) {
        if (sort.isUnsorted()) return posts;

        return posts.stream()
                .sorted((p1, p2) -> {
                    for (Sort.Order order : sort) {
                        Comparator<Post> comparator = getComparator(order);
                        int result = comparator.compare(p1, p2);
                        if (result != 0) return result;
                    }
                    return 0;
                })
                .collect(Collectors.toList());
    }

    // 페이징 처리 메서드
    private Page<PostResponse> paginateList(List<Post> allPosts, Pageable pageable) {
        int total = allPosts.size();
        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int start = currentPage * pageSize;

        if (start >= total) {
            return new PageImpl<>(Collections.emptyList(), pageable, total);
        }

        int end = Math.min(start + pageSize, total);
        List<Post> pageContent = allPosts.subList(start, end);

        // DTO 변환
        List<PostResponse> content = pageContent.stream()
                .map(PostResponse::from)
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, total);
    }

    // Comparator 생성기
//    private Comparator<Post> getComparator(Sort.Order order) {
//        return switch (order.getProperty()) {
//            case "createdDate" -> order.isAscending() ?
//                    Comparator.comparing(
//                            Post::getCreatedDate,
//                            Comparator.nullsLast(Comparator.naturalOrder())
//                    ) :
//                    Comparator.comparing(
//                            Post::getCreatedDate,
//                            Comparator.nullsFirst(Comparator.reverseOrder())
//                    );
//            case "viewCount" -> order.isAscending() ?
//                    Comparator.comparing(Post::getViewCount) :
//                    Comparator.comparing(Post::getViewCount).reversed();
//            default -> (p1, p2) -> 0;
//        };
//    }
    private Comparator<Post> getComparator(Sort.Order order) {
        return switch (order.getProperty()) {
            case "createdDate" -> order.isAscending() ?
                    Comparator.comparing(
                            Post::getCreatedDate,
                            Comparator.nullsLast(Comparator.naturalOrder())
                    ) :
                    Comparator.comparing(
                            Post::getCreatedDate,
                            Comparator.nullsFirst(Comparator.reverseOrder())
                    );
            case "viewCount" -> order.isAscending() ?
                    Comparator.comparing(Post::getViewCount) :
                    Comparator.comparing(Post::getViewCount).reversed();
            case "title" -> order.isAscending() ?
                    Comparator.comparing(Post::getTitle, String.CASE_INSENSITIVE_ORDER) :
                    Comparator.comparing(Post::getTitle, String.CASE_INSENSITIVE_ORDER).reversed();
            default -> Comparator.comparing(Post::getId);
        };
    }
}
