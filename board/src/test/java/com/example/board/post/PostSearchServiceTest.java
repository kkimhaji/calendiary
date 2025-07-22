package com.example.board.post;

import com.example.board.category.TeamCategory;
import com.example.board.permission.CategoryPermission;
import com.example.board.post.dto.PostResponse;
import com.example.board.post.enums.SearchType;
import com.example.board.support.AbstractTestSupport;
import com.example.board.support.TestDataBuilder;
import com.example.board.team.Team;
import com.example.board.teamMember.TeamMember;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class PostSearchServiceTest extends AbstractTestSupport {
    @Autowired
    private PostSearchService postSearchService;
    @Autowired
    private TestDataBuilder testDataBuilder;
    @Autowired
    private PostRepository postRepository;
    private Team testTeam;
    private TeamCategory category1;
    private TeamCategory category2;

    private Post post1;
    private Post post2;
    private Post post3;
    private Post post4;
    private TeamMember teamMember1;
    private TeamMember teamMember2;

    @BeforeEach
    void init() throws InterruptedException {
        testTeam = testDataBuilder.createTeam(member1);
        teamMember1 = testDataBuilder.getTeamMember(testTeam.getId(), member1.getMemberId());
        teamMember2 = testDataBuilder.addMemberToTeam(member2, testTeam.getId());
        category1 = testDataBuilder.createCategory(testTeam.getId(), "프로그래밍",
                Set.of(CategoryPermission.CREATE_POST, CategoryPermission.VIEW_POST));
        category2 = testDataBuilder.createCategory(testTeam.getId(), "디자인",
                Set.of(CategoryPermission.CREATE_POST, CategoryPermission.VIEW_POST));
        post1 = testDataBuilder.createPost(
                "Java 기초 강의",
                "이 강의에서는 Java의 기본 문법을 배웁니다",
                member1, category1, testTeam, teamMember1);
        post1.increaseViewCount(); // 조회수 1
        postRepository.save(post1);
        Thread.sleep(100); // 시간차

        post2 = testDataBuilder.createPost(
                "Spring Boot 실습",
                "Spring Boot를 이용한 웹 애플리케이션 개발 실습입니다",
                member2, category1, testTeam, teamMember2);
        post2.increaseViewCount();
        post2.increaseViewCount(); // 조회수 2
        postRepository.save(post2);
        Thread.sleep(100);

        post3 = testDataBuilder.createPost(
                "데이터베이스 설계",
                "효율적인 데이터베이스 스키마 설계 방법론",
                member1, category1, testTeam, teamMember1);
        post3.increaseViewCount();
        post3.increaseViewCount();
        post3.increaseViewCount(); // 조회수 3
        postRepository.save(post3);

        Thread.sleep(100);

        post4 = testDataBuilder.createPost(
                "React 프론트엔드",
                "React를 이용한 모던 프론트엔드 개발",
                member2, category2, testTeam, teamMember2);
        // 조회수 0
        postRepository.save(post4);
    }

    @Test
    @DisplayName("검색 성공 - 제목으로 검색")
    void searchPosts_byTitle_success() {
        // given
        String keyword = "Java";
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdDate").descending());

        // when
        Page<PostResponse> result = postSearchService.searchPosts(
                testTeam.getId(), keyword, null, pageable, SearchType.TITLE);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("Java 기초 강의");
        assertThat(result.getTotalElements()).isEqualTo(1L);
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isTrue();
    }

    @Test
    @DisplayName("검색 성공 - 내용으로 검색")
    void searchPosts_byContent_success() {
        // given
        String keyword = "웹 애플리케이션";
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<PostResponse> result = postSearchService.searchPosts(
                testTeam.getId(), keyword, null, pageable, SearchType.CONTENT);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("Spring Boot 실습");
        assertThat(result.getContent().get(0).content()).contains("웹 애플리케이션");
    }

    @Test
    @DisplayName("검색 성공 - 제목 또는 내용으로 검색")
    void searchPosts_byTitleOrContent_success() {
        // given
        String keyword = "Spring"; // 제목과 내용에 모두 있을 수 있는 키워드
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdDate").descending());

        // when
        Page<PostResponse> result = postSearchService.searchPosts(
                testTeam.getId(), keyword, null, pageable, SearchType.BOTH);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("Spring Boot 실습");
    }

    @Test
    @DisplayName("검색 성공 - 기본값(BOTH) 검색")
    void searchPosts_defaultSearchType_success() {
        // given
        String keyword = "개발";
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<PostResponse> result = postSearchService.searchPosts(
                testTeam.getId(), keyword, null, pageable, null); // null로 default 테스트

        // then
        assertThat(result.getContent()).hasSize(2); // "개발"이 포함된 게시글들
        List<String> titles = result.getContent().stream()
                .map(PostResponse::title)
                .toList();
        assertThat(titles).containsExactlyInAnyOrder("Spring Boot 실습", "React 프론트엔드");
    }

    @Test
    @DisplayName("검색 실패 - 키워드가 null인 경우")
    void searchPosts_nullKeyword_emptyResult() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<PostResponse> result = postSearchService.searchPosts(
                testTeam.getId(), null, null, pageable, SearchType.BOTH);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0L);
    }

    @Test
    @DisplayName("검색 실패 - 키워드가 빈 문자열인 경우")
    void searchPosts_emptyKeyword_emptyResult() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<PostResponse> result = postSearchService.searchPosts(
                testTeam.getId(), "   ", null, pageable, SearchType.BOTH);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0L);
    }

    @Test
    @DisplayName("검색 성공 - 검색 결과 없음")
    void searchPosts_noResults_emptyPage() {
        // given
        String keyword = "존재하지않는키워드";
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<PostResponse> result = postSearchService.searchPosts(
                testTeam.getId(), keyword, null, pageable, SearchType.BOTH);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0L);
        assertThat(result.getTotalPages()).isEqualTo(0);
    }

    @Test
    @DisplayName("특정 카테고리에서만 검색")
    void searchPosts_specificCategory_success() {
        // given
        String keyword = "프론트엔드";
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<PostResponse> result = postSearchService.searchPosts(
                testTeam.getId(), keyword, category2.getId(), pageable, SearchType.BOTH);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("React 프론트엔드");
        assertThat(result.getContent().get(0).categoryName()).isEqualTo("디자인");
    }

    @Test
    @DisplayName("다른 카테고리에서 검색 - 결과 없음")
    void searchPosts_wrongCategory_noResults() {
        // given
        String keyword = "Java"; // category1에 있는 키워드
        Pageable pageable = PageRequest.of(0, 10);

        // when - category2에서 검색
        Page<PostResponse> result = postSearchService.searchPosts(
                testTeam.getId(), keyword, category2.getId(), pageable, SearchType.BOTH);

        // then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("모든 카테고리에서 검색")
    void searchPosts_allCategories_success() {
        // given
        String keyword = "설계"; // category1에 있는 키워드
        Pageable pageable = PageRequest.of(0, 10);

        // when - categoryId null로 모든 카테고리 검색
        Page<PostResponse> result = postSearchService.searchPosts(
                testTeam.getId(), keyword, null, pageable, SearchType.BOTH);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("데이터베이스 설계");
    }

    @Test
    @DisplayName("생성일 기준 내림차순 정렬")
    void searchPosts_sortByCreatedDateDesc_success() {
        String keyword = "a"; // 모든 게시글 제목에 포함된 문자
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdDate").descending());

        // when
        Page<PostResponse> result = postSearchService.searchPosts(
                testTeam.getId(), keyword, null, pageable, SearchType.BOTH);

        // then
        assertThat(result.getContent()).hasSizeGreaterThanOrEqualTo(2);

        // 최신 게시글이 먼저 와야 함
        List<LocalDateTime> createdDates = result.getContent().stream()
                .map(PostResponse::createdDate)
                .toList();

        for (int i = 0; i < createdDates.size() - 1; i++) {
            assertThat(createdDates.get(i)).isAfterOrEqualTo(createdDates.get(i + 1));
        }
    }

    @Test
    @DisplayName("생성일 기준 오름차순 정렬")
    void searchPosts_sortByCreatedDateAsc_success() {
        // given
        String keyword = "기";
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdDate").ascending());

        // when
        Page<PostResponse> result = postSearchService.searchPosts(
                testTeam.getId(), keyword, null, pageable, SearchType.BOTH);

        // then
        List<LocalDateTime> createdDates = result.getContent().stream()
                .map(PostResponse::createdDate)
                .toList();

        for (int i = 0; i < createdDates.size() - 1; i++) {
            assertThat(createdDates.get(i)).isBeforeOrEqualTo(createdDates.get(i + 1));
        }
    }

    @Test
    @DisplayName("조회수 기준 내림차순 정렬")
    void searchPosts_sortByViewCountDesc_success() {
        // given
        String keyword = "데이터"; // 여러 게시글에 있을 키워드
        Pageable pageable = PageRequest.of(0, 10, Sort.by("viewCount").descending());

        // when
        Page<PostResponse> result = postSearchService.searchPosts(
                testTeam.getId(), keyword, null, pageable, SearchType.BOTH);

        // then
        if (result.getContent().size() > 1) {
            List<Integer> viewCounts = result.getContent().stream()
                    .map(PostResponse::viewCount)
                    .toList();

            for (int i = 0; i < viewCounts.size() - 1; i++) {
                assertThat(viewCounts.get(i)).isGreaterThanOrEqualTo(viewCounts.get(i + 1));
            }
        }
    }

    @Test
    @DisplayName("조회수 기준 오름차순 정렬")
    void searchPosts_sortByViewCountAsc_success() {
        // given
        String keyword = "a"; // 모든 게시글에 포함될 키워드
        Pageable pageable = PageRequest.of(0, 10, Sort.by("viewCount").ascending());

        // when
        Page<PostResponse> result = postSearchService.searchPosts(
                testTeam.getId(), keyword, null, pageable, SearchType.BOTH);

        // then
        if (result.getContent().size() > 1) {
            List<Integer> viewCounts = result.getContent().stream()
                    .map(PostResponse::viewCount)
                    .toList();

            for (int i = 0; i < viewCounts.size() - 1; i++) {
                assertThat(viewCounts.get(i)).isLessThanOrEqualTo(viewCounts.get(i + 1));
            }
        }
    }

    @Test
    @DisplayName("다중 정렬 - 조회수 내림차순, 생성일 내림차순")
    void searchPosts_multipleSort_success() {
        // given
        String keyword = "a";
        Sort multiSort = Sort.by(
                Sort.Order.desc("viewCount"),
                Sort.Order.desc("createdDate")
        );
        Pageable pageable = PageRequest.of(0, 10, multiSort);

        // when
        Page<PostResponse> result = postSearchService.searchPosts(
                testTeam.getId(), keyword, null, pageable, SearchType.BOTH);

        // then
        assertThat(result.getContent()).hasSizeGreaterThan(1);

        // 조회수가 높은 순으로, 같으면 최신순으로 정렬되어야 함
        List<PostResponse> posts = result.getContent();
        for (int i = 0; i < posts.size() - 1; i++) {
            PostResponse current = posts.get(i);
            PostResponse next = posts.get(i + 1);

            if (current.viewCount() == next.viewCount()) {
                // 조회수가 같으면 최신순
                assertThat(current.createdDate()).isAfterOrEqualTo(next.createdDate());
            } else {
                // 조회수가 다르면 높은 순
                assertThat(current.viewCount()).isGreaterThan(next.viewCount());
            }
        }
    }
}
