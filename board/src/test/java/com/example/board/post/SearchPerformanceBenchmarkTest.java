package com.example.board.post;

import com.example.board.category.CategoryService;
import com.example.board.category.TeamCategory;
import com.example.board.category.dto.CategoryRolePermissionDTO;
import com.example.board.category.dto.CreateCategoryRequest;
import com.example.board.member.Member;
import com.example.board.member.MemberRepository;
import com.example.board.permission.CategoryPermission;
import com.example.board.post.dto.PostResponse;
import com.example.board.post.enums.SearchType;
import com.example.board.support.TestDataBuilder;
import com.example.board.support.TestDataFactory;
import com.example.board.team.Team;
import com.example.board.team.TeamRepository;
import com.example.board.teamMember.TeamMember;
import com.example.board.teamMember.TeamMemberRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Tag("benchmark")
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional // 각 테스트 메서드 단위로 트랜잭션 관리
public class SearchPerformanceBenchmarkTest {

    private static final int DATA_SIZE = 500;
    private static final int WARMUP_COUNT = 5;
    private static final int MEASURE_COUNT = 20;
    private static final String KEYWORD = "Spring";

    @Autowired
    private PostSearchService postSearchService;
    @Autowired
    private TestDataFactory testDataFactory;
    @Autowired
    private TestDataBuilder testDataBuilder;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private TeamMemberRepository teamMemberRepository;
    @Autowired
    private CategoryService categoryService;

    private Member member1;
    private Team testTeam;
    private TeamCategory testCategory;
    private TeamMember testTeamMember;


    @BeforeEach
    void init() {
        // @Transactional 없음 → 각 @Transactional 메서드 호출마다 즉시 커밋
        member1 = testDataFactory.createMember("bench1@test.com", "bench1", "1234");
        testTeam = testDataBuilder.createTeam(member1);
        testTeamMember = testDataBuilder.getTeamMember(testTeam.getId(), member1.getMemberId());
        testCategory = categoryService.createCategory(testTeam.getId(), new CreateCategoryRequest(
                "벤치마크",
                "벤치마크용 카테고리",
                List.of(
                        new CategoryRolePermissionDTO(
                                testTeam.getAdminRoleId(),
                                Set.of(CategoryPermission.values())),
                        new CategoryRolePermissionDTO(
                                testTeam.getBasicRoleId(),
                                Set.of(CategoryPermission.VIEW_POST, CategoryPermission.CREATE_POST))
                )
        ));
        seedPosts();
        // 비동기 스레드가 데이터를 읽을 수 있도록 커밋
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start(); // AfterEach에서 종료할 트랜잭션
    }

    @AfterEach
    void cleanup() {
        if (TestTransaction.isActive()) {
            TestTransaction.flagForRollback();
            TestTransaction.end();
        }
    }

    @Test
    @Order(1)
    @DisplayName("BOTH(단일 쿼리) vs PARALLEL(병렬 쿼리) 성능 비교")
    void benchmarkBothVsParallel() {
        PageRequest pageable = PageRequest.of(0, 20, Sort.by("createdDate").descending());

        System.out.println("\n[Warmup] " + WARMUP_COUNT + "회 실행 중...");
        for (int i = 0; i < WARMUP_COUNT; i++) {
            postSearchService.searchPosts(testTeam.getId(), KEYWORD, null, pageable, SearchType.BOTH);
            postSearchService.searchPosts(testTeam.getId(), KEYWORD, null, pageable, SearchType.PARALLEL);
        }

        long[] bothNs = measure(() -> postSearchService.searchPosts(testTeam.getId(), KEYWORD, null, pageable, SearchType.BOTH));
        long[] parallelNs = measure(() -> postSearchService.searchPosts(testTeam.getId(), KEYWORD, null, pageable, SearchType.PARALLEL));

        Page<PostResponse> bothResult = postSearchService.searchPosts(testTeam.getId(), KEYWORD, null, pageable, SearchType.BOTH);
        Page<PostResponse> parallelResult = postSearchService.searchPosts(testTeam.getId(), KEYWORD, null, pageable, SearchType.PARALLEL);

        Assertions.assertEquals(
                bothResult.getTotalElements(),
                parallelResult.getTotalElements(),
                "BOTH와 PARALLEL의 전체 결과 수가 동일해야 합니다."
        );

        printReport(bothNs, parallelNs, bothResult.getTotalElements());
    }

    private void seedPosts() {
        String[] titles = {
                "Spring Boot 입문", "Java 기초", "React 실전", "Docker 배포",
                "MySQL 최적화", "JPA 심화", "REST API 설계", "보안 가이드"
        };
        String[] contents = {
                "Spring Framework를 활용한 백엔드 개발 실습입니다. %d",
                "Java 객체지향 프로그래밍의 핵심 개념을 다룹니다. %d",
                "웹 애플리케이션 개발의 기초부터 심화까지 학습합니다. %d"
        };

        for (int i = 0; i < DATA_SIZE; i++) {
            String title = titles[i % titles.length] + " " + i;
            String content = String.format(contents[i % contents.length], i);
            testDataBuilder.createPost(title, content, member1, testCategory, testTeam, testTeamMember);
        }
    }

    private long[] measure(Runnable task) {
        long[] times = new long[MEASURE_COUNT];
        for (int i = 0; i < MEASURE_COUNT; i++) {
            long start = System.nanoTime();
            task.run();
            times[i] = System.nanoTime() - start;
        }
        return times;
    }

    private void printReport(long[] bothNs, long[] parallelNs, long hitCount) {
        Stats both = Stats.of(bothNs);
        Stats parallel = Stats.of(parallelNs);
        double improvement = (both.avgMs - parallel.avgMs) / both.avgMs * 100;

        System.out.printf("""
                        %n
                        검색 성능 벤치마크 결과
                        환경   : H2 인메모리 DB
                        데이터 : 게시글 %d건 / 키워드 '%s'
                        히트   : %d건
                        반복   : %d회 (워밍업 %d회 제외)
                        
                        [단일 쿼리 - BOTH]
                        Min  : %.2f ms
                        Avg  : %.2f ms
                        P95  : %.2f ms
                        Max  : %.2f ms
                        
                        [병렬 쿼리 - PARALLEL]
                        Min  : %.2f ms
                        Avg  : %.2f ms
                        P95  : %.2f ms
                        Max  : %.2f ms
                        
                        개선율 : %+.1f%% %-30s
                        %n""",
                DATA_SIZE, KEYWORD, hitCount,
                MEASURE_COUNT, WARMUP_COUNT,
                both.minMs, both.avgMs, both.p95Ms, both.maxMs,
                parallel.minMs, parallel.avgMs, parallel.p95Ms, parallel.maxMs,
                improvement, improvement > 0 ? "(PARALLEL이 빠름)" : "(BOTH가 빠름)"
        );
    }

    private record Stats(double minMs, double avgMs, double p95Ms, double maxMs) {
        static Stats of(long[] ns) {
            long[] sorted = Arrays.copyOf(ns, ns.length);
            Arrays.sort(sorted);
            double toMs = 1_000_000.0;
            return new Stats(
                    sorted[0] / toMs,
                    Arrays.stream(ns).average().orElse(0) / toMs,
                    sorted[(int) Math.ceil(0.95 * sorted.length) - 1] / toMs,
                    sorted[sorted.length - 1] / toMs
            );
        }
    }
}