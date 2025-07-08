package com.example.board.post;

import com.example.board.support.AbstractTestSupport;
import com.example.board.support.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;


@EnableAsync
public class PostViewCountTest extends AbstractTestSupport {

    @Autowired
    private PostService postService;
    @MockBean
    private PostRepository postRepository;
    @Autowired
    private TestDataBuilder testDataBuilder;

    @BeforeEach
    void init() {
        postService.clearViewCountCache();
    }

    @Test
    void increaseViewCount_success() throws Exception {
        // given
        Long postId = 1L;

        // when
        CompletableFuture<Void> future = postService.increaseViewCount(postId);
        future.get(2, TimeUnit.SECONDS); // 비동기 완료 대기

        // then
        assertThat(postService.getCachedViewCount(postId)).isEqualTo(1);

        // 임계값 미달로 DB 업데이트는 호출되지 않아야 함
        verify(postRepository, never()).updateViewCount(any(Long.class), anyLong());
    }

    @Test
    void increaseViewCount_reachThreshold() throws Exception {
        // given
        Long postId = 1L;

        // when - 10번 증가시켜 임계값 도달
        for (int i = 0; i < 10; i++) {
            CompletableFuture<Void> future = postService.increaseViewCount(postId);
            future.get(1, TimeUnit.SECONDS);
        }

        // then
        // 캐시가 0으로 초기화되어야 함
        assertThat(postService.getCachedViewCount(postId)).isEqualTo(0);

        // DB 업데이트가 호출되어야 함
        verify(postRepository, times(1)).updateViewCount(postId, 10L);
    }

    @Test
    void increaseViewCount_beforeThreshold() throws Exception {
        // given
        Long postId = 1L;

        // when - 9번만 증가 (임계값 직전)
        for (int i = 0; i < 9; i++) {
            CompletableFuture<Void> future = postService.increaseViewCount(postId);
            future.get(1, TimeUnit.SECONDS);
        }

        // then
        assertThat(postService.getCachedViewCount(postId)).isEqualTo(9);
        verify(postRepository, never()).updateViewCount(any(Long.class), anyLong());
    }

    @Test
    void increaseViewCount_multiplePost() throws Exception {
        // given
        Long postId1 = 1L;
        Long postId2 = 2L;
        Long postId3 = 3L;

        // when
        List<CompletableFuture<Void>> futures = Arrays.asList(
                postService.increaseViewCount(postId1),
                postService.increaseViewCount(postId2),
                postService.increaseViewCount(postId1), // postId1 한 번 더
                postService.increaseViewCount(postId3)
        );

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .get(3, TimeUnit.SECONDS);

        // then
        assertThat(postService.getCachedViewCount(postId1)).isEqualTo(2);
        assertThat(postService.getCachedViewCount(postId2)).isEqualTo(1);
        assertThat(postService.getCachedViewCount(postId3)).isEqualTo(1);

        // ArgumentMatcher 사용: 임계값 미달로 DB 업데이트 호출 안됨
        verify(postRepository, never()).updateViewCount(anyLong(), anyLong());
    }

    @Test
    void syncSinglePostViewCount_success() {
        // given
        Long postId = 1L;
        postService.setCachedViewCount(postId, 5L);

        // when
        postService.syncSinglePostViewCount(postId);

        // then
        // 캐시가 0으로 초기화되어야 함
        assertThat(postService.getCachedViewCount(postId)).isEqualTo(0);

        // DB 업데이트 호출 확인
        verify(postRepository).updateViewCount(postId, 5L);
        verify(postRepository).updateViewCount(eq(postId), eq(5L));

    }

    @Test
    void syncSinglePostViewCount_zeroCount() {
        // given
        Long postId = 1L;
        postService.setCachedViewCount(postId, 0L);

        // when
        postService.syncSinglePostViewCount(postId);

        // then
        // DB 업데이트가 호출되지 않아야 함
        verify(postRepository, never()).updateViewCount(any(Long.class), anyLong());
    }

    @Test
    void syncSinglePostViewCount_notInCache() {
        // given
        Long postId = 999L; // 캐시에 없는 postId

        // when
        postService.syncSinglePostViewCount(postId);

        // then
        verify(postRepository, never()).updateViewCount(anyLong(), anyLong());

    }

    @Test
    @DisplayName("모든 캐시된 조회수 DB 동기화")
    void syncViewCountsToDatabase_success() {
        // given
        postService.setCachedViewCount(1L, 3L);
        postService.setCachedViewCount(2L, 7L);
        postService.setCachedViewCount(3L, 0L); // 0인 경우는 업데이트하지 않음

        // when
        postService.syncViewCountsToDatabase();

        // then
        // 모든 캐시가 0으로 초기화되어야 함
        assertThat(postService.getCachedViewCount(1L)).isEqualTo(0);
        assertThat(postService.getCachedViewCount(2L)).isEqualTo(0);
        assertThat(postService.getCachedViewCount(3L)).isEqualTo(0);

        // 0이 아닌 조회수만 DB 업데이트 호출
        verify(postRepository).updateViewCount(eq(1L), eq(3L));
        verify(postRepository).updateViewCount(eq(2L), eq(7L));
        verify(postRepository, never()).updateViewCount(eq(3L), anyLong());
    }

}