package com.example.board.domain.post;

import com.example.board.domain.member.Member;
import com.example.board.domain.team.Team;
import com.example.board.domain.team.TeamCategory;
import com.example.board.dto.post.PostListResponse;
import com.example.board.dto.post.PostSummaryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByAuthor(Member member);
    List<Post> findAllByCategory(TeamCategory category);

    // 팀의 카테고리별 게시글 조회
    @Query("SELECT new com.example.board.dto.post.PostListResponse(" +
            "p.id, p.title, p.author.nickname, " +
            "p.category.name, p.category.id, p.viewCount, p.createdDate, " +
            "(SELECT COUNT(c) FROM Comment c WHERE c.post.id = p.id)) " +
            "FROM Post p " +
            "WHERE p.team.id = :teamId " +
            "AND p.category.id = :categoryId " +
            "ORDER BY p.createdDate DESC")
    Page<PostListResponse> findByTeamAndCategory(
            @Param("teamId") Long teamId,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );

    // 팀의 최근 게시글 목록 조회
    @Query("SELECT new com.example.board.dto.post.PostListResponse(" +
            "p.id, p.title, p.author.nickname, p.category.name, p.category.id, p.viewCount, p.createdDate, " +
            "(SELECT COUNT(c) FROM Comment c WHERE c.post.id = p.id)) " +
            "FROM Post p " +
            "WHERE p.team.id = :teamId " +
            "ORDER BY p.createdDate DESC")
    Page<PostListResponse> findRecentPostsByTeamId(
            @Param("teamId") Long teamId,
            Pageable pageable
    );

    // 게시글 상세 조회
    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.author " +
            "WHERE p.id = :postId")
    Optional<Post> findByIdWithAuthor(
            @Param("postId") Long postId
    );

    // 최근 게시글 요약 정보
    @Query("SELECT new com.example.board.dto.post.PostSummaryDTO(" +
            "p.id, p.title, p.createdDate) " +
            "FROM Post p " +
            "WHERE p.category.id = :categoryId " +
            "ORDER BY p.createdDate DESC")
    List<PostSummaryDTO> findRecentPostsByCategoryId(
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );

    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + :count WHERE p.id = :postId")
    void updateViewCount(@Param("postId") Long postId, @Param("count") long count);

    @Query("SELECT p FROM Post p " +
            "LEFT JOIN FETCH p.author " +
            "LEFT JOIN FETCH p.category " +
            "WHERE p.title LIKE %:keyword% " +
            "AND p.team.id = :teamId")
    Page<Post> findByTitleContainingAndTeamId(
            @Param("keyword") String keyword,
            @Param("teamId") Long teamId,
            Pageable pageable
    );

    @Query("SELECT p FROM Post p " +
            "LEFT JOIN FETCH p.author " +
            "LEFT JOIN FETCH p.category " +
            "WHERE p.content LIKE %:keyword% " +
            "AND p.team.id = :teamId")
    Page<Post> findByContentContainingAndTeamId(
            @Param("keyword") String keyword,
            @Param("teamId") Long teamId,
            Pageable pageable
    );

    // 카테고리 필터링 추가된 메서드
    @Query("SELECT p FROM Post p " +
            "LEFT JOIN FETCH p.author " +
            "LEFT JOIN FETCH p.category " +
            "WHERE p.title LIKE %:keyword% " +
            "AND p.team.id = :teamId " +
            "AND (:categoryId IS NULL OR p.category.id = :categoryId)")
    Page<Post> searchByTitle(
            @Param("keyword") String keyword,
            @Param("teamId") Long teamId,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );

    @Query("SELECT p FROM Post p " +
            "LEFT JOIN FETCH p.author " +
            "LEFT JOIN FETCH p.category " +
            "WHERE p.content LIKE %:keyword% " +
            "AND p.team.id = :teamId " +
            "AND (:categoryId IS NULL OR p.category.id = :categoryId)")
    Page<Post> searchByContent(
            @Param("keyword") String keyword,
            @Param("teamId") Long teamId,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );

    // 카테고리 내 제목 검색
    @Query("SELECT p FROM Post p " +
            "LEFT JOIN FETCH p.author " +
            "WHERE p.title LIKE %:keyword% " +
            "AND p.team.id = :teamId " +
            "AND p.category.id = :categoryId")
    Page<Post> findByTitleInCategory(
            @Param("keyword") String keyword,
            @Param("teamId") Long teamId,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );

    // 카테고리 내 내용 검색
    @Query("SELECT p FROM Post p " +
            "LEFT JOIN FETCH p.author " +
            "WHERE p.content LIKE %:keyword% " +
            "AND p.team.id = :teamId " +
            "AND p.category.id = :categoryId")
    Page<Post> findByContentInCategory(
            @Param("keyword") String keyword,
            @Param("teamId") Long teamId,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );
}