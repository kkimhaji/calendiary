package com.example.board.post;

import com.example.board.support.AbstractTestSupport;
import com.example.board.support.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
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
    @DisplayName("기본 조회수 증가 동작 확인")
    void increaseViewCount_basicTest() throws Exception {
        // given
        Long postId = 1L;

        // when - 단일 호출로 기본 동작 확인
        CompletableFuture<Void> future = postService.increaseViewCount(postId);
        future.get(5, TimeUnit.SECONDS);

        // then
        long cachedCount = postService.getCachedViewCount(postId);
        System.out.println("Cached count after 1 call: " + cachedCount); // 디버깅용

        assertThat(cachedCount).isEqualTo(1);
    }

    @Test
    @DisplayName("순차적 조회수 증가 테스트")
    void increaseViewCount_sequential() throws Exception {
        // given
        Long postId = 1L;

        // when - 순차적으로 5번 호출
        for (int i = 0; i < 5; i++) {
            CompletableFuture<Void> future = postService.increaseViewCount(postId);
            future.get(3, TimeUnit.SECONDS);

            long currentCount = postService.getCachedViewCount(postId);
            System.out.println("After call " + (i + 1) + ": " + currentCount);

            assertThat(currentCount).isEqualTo(i + 1);
        }
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

    @Test
    @DisplayName("비동기 동작 디버깅")
    void debugAsyncBehavior() throws Exception {
        // given
        Long postId = 1L;

        System.out.println("Test started");
        System.out.println("Initial cache count: " + postService.getCachedViewCount(postId));

        // when
        CompletableFuture<Void> future = postService.increaseViewCount(postId);
        System.out.println("Future created");

        // 완료 대기 중 상태 확인
        boolean completed = future.get(5, TimeUnit.SECONDS) == null;
        System.out.println("Future completed: " + completed);
        System.out.println("Final cache count: " + postService.getCachedViewCount(postId));

        // 추가 대기
        Thread.sleep(1000);
        System.out.println("After sleep cache count: " + postService.getCachedViewCount(postId));
    }

    @Test
    @DisplayName("임계값 도달 테스트 - 동시성 없음")
    void increaseViewCount_reachThreshold_sequential() throws Exception {
        // given
        Long postId = 1L;

        // when - 순차적으로 10번 호출
        for (int i = 0; i < 10; i++) {
            CompletableFuture<Void> future = postService.increaseViewCount(postId);
            future.get(2, TimeUnit.SECONDS);

            System.out.println("Call " + (i + 1) + " - Cached count: " +
                    postService.getCachedViewCount(postId));
        }

        // then
        await().atMost(5, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    long cachedCount = postService.getCachedViewCount(postId);
                    // 10번 호출 시 임계값 도달로 캐시가 0이 되어야 함
                    assertThat(cachedCount).isEqualTo(0);
                });

        verify(postRepository, times(1)).updateViewCount(eq(postId), eq(10L));
    }

    @Test
    @DisplayName("@Async 동작 확인")
    void checkAsyncBehavior() throws Exception {
        // given
        Long postId = 1L;
        String mainThread = Thread.currentThread().getName();
        System.out.println("Main thread: " + mainThread);

        // when
        CompletableFuture<Void> future = postService.increaseViewCount(postId);

        // @Async가 동작한다면 다른 스레드에서 실행되어야 함
        future.thenRun(() -> {
            String asyncThread = Thread.currentThread().getName();
            System.out.println("Async thread: " + asyncThread);
            System.out.println("Different thread: " + !mainThread.equals(asyncThread));
        });

        future.get(3, TimeUnit.SECONDS);
        Thread.sleep(1000);

        System.out.println("Final cache count: " + postService.getCachedViewCount(postId));
    }

    @Test
    @DisplayName("조회수 증가 - CompletableFuture 사용")
    void increaseViewCount_withCompletableFuture() throws Exception {
        // given
        Long postId = 1L;

        // when
        CompletableFuture<Void> future = postService.increaseViewCount(postId);
        future.get(3, TimeUnit.SECONDS);

        // then
        assertThat(postService.getCachedViewCount(postId)).isEqualTo(1);
    }


    @Test
    @DisplayName("복합 시나리오 - 증가와 동기화")
    void complexScenario_increaseAndSync() throws Exception {
        // given
        Long postId1 = 1L;
        Long postId2 = 2L;

        // when - postId1은 5번, postId2는 15번 증가
        for (int i = 0; i < 5; i++) {
            postService.increaseViewCount(postId1).get(1, TimeUnit.SECONDS);
        }

        for (int i = 0; i < 15; i++) {
            postService.increaseViewCount(postId2).get(1, TimeUnit.SECONDS);
        }

        // then
        assertThat(postService.getCachedViewCount(postId1)).isEqualTo(5);
        assertThat(postService.getCachedViewCount(postId2)).isEqualTo(5);

        // ArgumentMatcher 사용: postId2의 첫 번째 임계값 도달만 검증
        verify(postRepository, times(1)).updateViewCount(eq(postId2), eq(10L));
        verify(postRepository, never()).updateViewCount(eq(postId1), anyLong());

        // when - 전체 동기화 실행
        postService.syncViewCountsToDatabase();

        // then
        assertThat(postService.getCachedViewCount(postId1)).isEqualTo(0);
        assertThat(postService.getCachedViewCount(postId2)).isEqualTo(0);

        // ArgumentMatcher 사용: 추가 동기화 검증
        verify(postRepository).updateViewCount(eq(postId1), eq(5L));
        verify(postRepository, times(2)).updateViewCount(eq(postId2), anyLong()); // 10L, 5L 총 2회
    }
}