package com.example.board.post;

import com.example.board.member.Member;
import com.example.board.category.TeamCategory;
import com.example.board.post.dto.PostListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByAuthor(Member member);
    List<Post> findAllByCategory(TeamCategory category);

    @Query("SELECT p FROM Post p WHERE p.category.team.id = :teamId AND p.author.id = :authorId")
    List<Post> findAllByTeamIdAndAuthorId(@Param("teamId") Long teamId, @Param("authorId") Long authorId);

    // 팀의 카테고리별 게시글 조회
    @Query("SELECT new com.example.board.post.dto.PostListResponse(" +
            "p.id, p.title, " +
            "COALESCE(p.teamMember.teamNickname, 'Unknown'), " + // null 안전 처리
            "p.team.id, p.category.name, p.category.id, p.viewCount, p.createdDate, " +
            "(SELECT COUNT(c) FROM Comment c WHERE c.post.id = p.id)) " +
            "FROM Post p " +
            "LEFT JOIN p.teamMember tm " + // LEFT JOIN으로 변경
            "WHERE p.team.id = :teamId " +
            "AND p.category.id = :categoryId " +
            "ORDER BY p.createdDate DESC")
    Page<PostListResponse> findByTeamAndCategory(
            @Param("teamId") Long teamId,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );

    // 팀의 최근 게시글 목록 조회
    @Query("SELECT new com.example.board.post.dto.PostListResponse(" +
            "p.id, p.title, " +
            "COALESCE(p.teamMember.teamNickname, 'Unknown'), " + // null 안전 처리
            "p.team.id, p.category.name, p.category.id, p.viewCount, p.createdDate, " +
            "(SELECT COUNT(c) FROM Comment c WHERE c.post.id = p.id)) " +
            "FROM Post p " +
            "LEFT JOIN p.teamMember tm " + // LEFT JOIN으로 변경
            "WHERE p.team.id = :teamId " +
            "ORDER BY p.createdDate DESC")
    Page<PostListResponse> findRecentPostsByTeamId(
            @Param("teamId") Long teamId,
            Pageable pageable
    );

    //사용자의 작성 게시글 목록
    @Query("SELECT new com.example.board.post.dto.PostListResponse(" +
            "p.id, p.title, " +
            "COALESCE(p.teamMember.teamNickname, 'Unknown'), " +
            "p.team.id, c.name, c.id, p.viewCount, p.createdDate, " +
            "(SELECT COUNT(cm) FROM Comment cm WHERE cm.post = p)) " +
            "FROM Post p " +
            "JOIN p.teamMember tm " +  // teamMember 직접 조인
            "JOIN p.category c " +
            "WHERE p.teamMember.id = :teamMemberId")  // TeamMember 단독 기준
    Page<PostListResponse> findPostListResponseByTeamMemberId(
            @Param("teamMemberId") Long teamMemberId,
            Pageable pageable);


    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + :count WHERE p.id = :postId")
    void updateViewCount(@Param("postId") Long postId, @Param("count") long count);

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

    @Query("SELECT p FROM Post p " +
            "LEFT JOIN FETCH p.author " +
            "LEFT JOIN FETCH p.category " +
            "WHERE (p.title LIKE %:keyword% OR p.content LIKE %:keyword%) " +
            "AND p.team.id = :teamId " +
            "AND (:categoryId IS NULL OR p.category.id = :categoryId)")
    Page<Post> searchByTitleOrContent(
            @Param("keyword") String keyword,
            @Param("teamId") Long teamId,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );

    @Query("SELECT p FROM Post p " +
            "WHERE p.team.id IN :teamIds " +
            "ORDER BY p.createdDate DESC")
    Page<Post> findByTeamIds(
            @Param("teamIds") List<Long> teamIds,
            Pageable pageable
    );
}