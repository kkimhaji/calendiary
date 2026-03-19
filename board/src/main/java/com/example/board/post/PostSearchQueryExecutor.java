// board/src/main/java/com/example/board/post/PostSearchQueryExecutor.java
package com.example.board.post;

import com.example.board.post.dto.PostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 비동기 스레드에서 호출 시 Spring AOP가 새 트랜잭션을 생성하도록
 * PostSearchService와 빈을 분리한 실행자.
 *
 * PostResponse 변환까지 트랜잭션 안에서 수행해야
 * teamMember 등의 Lazy 필드 접근이 가능합니다.
 */
@Component
@RequiredArgsConstructor
public class PostSearchQueryExecutor {

    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    public List<PostResponse> searchByTitle(
            String keyword, Long teamId, Long categoryId, Pageable pageable) {

        return postRepository.searchByTitle(keyword, teamId, categoryId, pageable)
                .getContent()
                .stream()
                .map(PostResponse::from) // 트랜잭션 활성 상태 → Lazy 로딩 가능
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PostResponse> searchByContent(
            String keyword, Long teamId, Long categoryId, Pageable pageable) {

        return postRepository.searchByContent(keyword, teamId, categoryId, pageable)
                .getContent()
                .stream()
                .map(PostResponse::from) // 트랜잭션 활성 상태 → Lazy 로딩 가능
                .toList();
    }
}