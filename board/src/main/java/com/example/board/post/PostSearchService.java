package com.example.board.post;

import com.example.board.common.service.EntityValidationService;
import com.example.board.post.dto.PostResponse;
import com.example.board.post.enums.SearchType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.*;
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

    private static final int MAX_PARALLEL_FETCH_SIZE = 500;

    private final PostRepository postRepository;
    private final PostSearchQueryExecutor queryExecutor; // 트랜잭션 격리용
    @Qualifier("asyncTaskExecutor")
    private final Executor asyncExecutor;
    private final EntityValidationService validationService;

    public Page<PostResponse> searchPosts(
            Long teamId, String keyword, Long categoryId,
            Pageable pageable, SearchType searchType) {

        validationService.validateTeamExists(teamId);
        if (categoryId != null) validationService.validateCategoryExists(categoryId);
        if (searchType == null) searchType = SearchType.BOTH;
        if (keyword == null || keyword.isBlank()) return Page.empty(pageable);

        long start = System.nanoTime();

        Page<PostResponse> result = switch (searchType) {
            case TITLE    -> postRepository.searchByTitle(keyword, teamId, categoryId, pageable)
                    .map(PostResponse::from);
            case CONTENT  -> postRepository.searchByContent(keyword, teamId, categoryId, pageable)
                    .map(PostResponse::from);
            case PARALLEL -> searchInParallel(keyword, teamId, categoryId, pageable);
            default       -> postRepository.searchByTitleOrContent(keyword, teamId, categoryId, pageable)
                    .map(PostResponse::from);
        };

        log.info("[Search] type={}, keyword='{}', results={}, elapsed={}ms",
                searchType, keyword, result.getTotalElements(),
                (System.nanoTime() - start) / 1_000_000);

        return result;
    }

    // ─── 병렬 검색 ────────────────────────────────────────────────────────────

    private Page<PostResponse> searchInParallel(
            String keyword, Long teamId, Long categoryId, Pageable pageable) {

        Pageable fetchPageable = PageRequest.of(0, MAX_PARALLEL_FETCH_SIZE, pageable.getSort());

        // queryExecutor의 @Transactional이 비동기 스레드에서 새 트랜잭션을 생성
        // → PostResponse 변환까지 트랜잭션 안에서 완료됨
        CompletableFuture<List<PostResponse>> titleFuture = CompletableFuture
                .supplyAsync(
                        () -> queryExecutor.searchByTitle(keyword, teamId, categoryId, fetchPageable),
                        asyncExecutor)
                .exceptionally(ex -> {
                    log.warn("[Search][PARALLEL] 제목 검색 실패: {}", ex.getMessage());
                    return Collections.emptyList();
                });

        CompletableFuture<List<PostResponse>> contentFuture = CompletableFuture
                .supplyAsync(
                        () -> queryExecutor.searchByContent(keyword, teamId, categoryId, fetchPageable),
                        asyncExecutor)
                .exceptionally(ex -> {
                    log.warn("[Search][PARALLEL] 내용 검색 실패: {}", ex.getMessage());
                    return Collections.emptyList();
                });

        return processResults(titleFuture, contentFuture, pageable);
    }

    private Page<PostResponse> processResults(
            CompletableFuture<List<PostResponse>> titleFuture,
            CompletableFuture<List<PostResponse>> contentFuture,
            Pageable pageable) {

        CompletableFuture.allOf(titleFuture, contentFuture).join();

        List<PostResponse> combined = Stream
                .concat(titleFuture.join().stream(), contentFuture.join().stream())
                .collect(Collectors.toMap(
                        PostResponse::id,
                        response -> response,
                        (existing, duplicate) -> existing,
                        LinkedHashMap::new
                ))
                .values()
                .stream()
                .toList();

        List<PostResponse> sorted = sortResponses(combined, pageable.getSort());
        return paginateResponses(sorted, pageable);
    }

    // ─── 정렬 / 페이징 ────────────────────────────────────────────────────────

    private List<PostResponse> sortResponses(List<PostResponse> responses, Sort sort) {
        if (sort.isUnsorted()) return responses;

        return responses.stream()
                .sorted((r1, r2) -> {
                    for (Sort.Order order : sort) {
                        int result = getComparator(order).compare(r1, r2);
                        if (result != 0) return result;
                    }
                    return 0;
                })
                .toList();
    }

    private Page<PostResponse> paginateResponses(List<PostResponse> responses, Pageable pageable) {
        int total = responses.size();
        int start = (int) pageable.getOffset();

        if (start >= total) {
            return new PageImpl<>(Collections.emptyList(), pageable, total);
        }

        return new PageImpl<>(
                responses.subList(start, Math.min(start + pageable.getPageSize(), total)),
                pageable,
                total
        );
    }

    private Comparator<PostResponse> getComparator(Sort.Order order) {
        return switch (order.getProperty()) {
            case "createdDate" -> order.isAscending()
                    ? Comparator.comparing(PostResponse::createdDate)
                    : Comparator.comparing(PostResponse::createdDate).reversed();
            case "viewCount" -> order.isAscending()
                    ? Comparator.comparing(PostResponse::viewCount)
                    : Comparator.comparing(PostResponse::viewCount).reversed();
            default -> (r1, r2) -> 0;
        };
    }
}