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
    List<Post> findAllByTeam(Team team);
    List<Post> findAllByCategory(TeamCategory category);

    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.author " +
            "JOIN FETCH p.category " +
            "WHERE p.team.id = :teamId AND p.category.id = :categoryId")
    List<Post> findAllByTeamAndCategory(
            @Param("teamId") Long teamId,
            @Param("categoryId") Long categoryId
    );

    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.author " +
            "JOIN FETCH p.category " +
            "WHERE p.id = :postId AND p.team.id = :teamId")
    Optional<Post> findByIdAndTeamWithAuthorAndCategory(
            @Param("postId") Long postId,
            @Param("teamId") Long teamId
    );

    // 페이징 처리를 위한 메서드
    @Query(value = "SELECT p FROM Post p " +
            "JOIN FETCH p.author " +
            "JOIN FETCH p.category " +
            "WHERE p.team.id = :teamId AND p.category.id = :categoryId",
            countQuery = "SELECT COUNT(p) FROM Post p " +
                    "WHERE p.team.id = :teamId AND p.category.id = :categoryId")
    Page<Post> findAllByTeamAndCategoryWithPaging(
            @Param("teamId") Long teamId,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );

    // 카테고리의 게시글 목록 조회 (제목만)
    @Query(value="SELECT new com.example.board.dto.post.PostSummaryDTO(p.id, p.title, p.createdDate) " +
            "FROM Post p " +
            "WHERE p.category.id = :categoryId " +
            "ORDER BY p.createdDate DESC",
            countQuery = "SELECT COUNT(p) FROM Post p WHERE p.category.id = :categoryId")
    Page<PostSummaryDTO> findPostSummariesByCategoryId(
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );

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
            "WHERE (p.title LIKE %:keyword% OR p.content LIKE %:keyword%) " +
            "AND p.team.id = :teamId")
    Page<Post> searchByTeamAndKeyword(
            @Param("teamId") Long teamId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("SELECT p FROM Post p " +
            "WHERE p.title LIKE %:keyword% " +
            "AND p.team.id = :teamId")
    Page<Post> findByTitleContainingAndTeamId(
            @Param("keyword") String keyword,
            @Param("teamId") Long teamId,
            Pageable pageable
    );

    @Query("SELECT p FROM Post p " +
            "WHERE p.content LIKE %:keyword% " +
            "AND p.team.id = :teamId")
    Page<Post> findByContentContainingAndTeamId(
            @Param("keyword") String keyword,
            @Param("teamId") Long teamId,
            Pageable pageable
    );
}