package com.example.board.domain.comment;

import com.example.board.dto.comment.CommentResponse;
import com.example.board.dto.comment.MemberCommentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("SELECT c FROM Comment c " +
            "LEFT JOIN FETCH c.replies " +
            "JOIN FETCH c.author " +
            "WHERE c.post.id = :postId " +
            "AND c.parent IS NULL " +
            "ORDER BY c.createdDate ASC")
    List<Comment> findAllByPostIdWithReplies(@Param("postId") Long postId);

    @Query("SELECT new com.example.board.dto.comment.CommentResponse(" +
            "c.id, " +
            "c.content, " +
            "c.author.id, " +
            "COALESCE(c.teamMember.teamNickname, 'Unknown'), " +
            "c.createdDate, " +
            "c.isDeleted, " +
            "null) " +
            "FROM Comment c " +
            "LEFT JOIN c.teamMember tm " +
            "JOIN c.author " +
            "WHERE c.post.id = :postId " +
            "AND c.parent IS NULL " +
            "ORDER BY c.createdDate ASC")
    List<CommentResponse> findAllByPostIdWithRepliesAsDto(@Param("postId") Long postId);


    @Query("SELECT COUNT(c) FROM Comment c " +
            "WHERE c.post.id = :postId")
    long countByPostId(@Param("postId") Long postId);

    Optional<Comment> findById(Long id);

    // 부모 댓글 없는 최상위 댓글만
    List<Comment> findByPostIdAndParentIsNull(Long postId);

    @Query("SELECT c FROM Comment c " +
            "LEFT JOIN FETCH c.author " +
            "LEFT JOIN FETCH c.replies " +
            "WHERE c.post.id = :postId AND c.parent IS NULL")
    List<Comment> findByPostIdWithAuthorAndReplies(@Param("postId") Long postId);

    void deleteAllByPostId(Long postId);

    void deleteAllByPostIdIn(List<Long> postIds);

    @Modifying
    @Transactional
    @Query("DELETE FROM Comment c WHERE c.post.category.team.id = :teamId AND c.author.memberId = :memberId")
    void deleteAllByTeamIdAndMemberId(@Param("teamId") Long teamId, @Param("memberId") Long memberId);

    @Query("SELECT c FROM Comment c WHERE c.post.category.team.id = :teamId AND c.author.id = :authorId")
    Page<Comment> findByTeamIdAndAuthorId(
            @Param("teamId") Long teamId,
            @Param("authorId") Long authorId,
            Pageable pageable);

    @Query("SELECT new com.example.board.dto.comment.MemberCommentResponse(" +
            "c.id, c.content, c.author.id, " +
            "COALESCE(c.teamMember.teamNickname, c.author.nickname, '익명'), " + // teamNickname 우선 사용, null이면 기본 닉네임
            "c.createdDate, c.isDeleted, " +
            "p.id, p.title, cat.id, t.id) " +
            "FROM Comment c " +
            "LEFT JOIN c.teamMember tm " + // teamMember에 LEFT JOIN 추가
            "JOIN c.post p " +
            "JOIN p.category cat " +
            "JOIN cat.team t " +
            "WHERE c.author.memberId = :memberId AND t.id = :teamId " +
            "ORDER BY c.createdDate DESC")
    Page<MemberCommentResponse> findCommentsByMemberIdAndTeamId(
            @Param("memberId") Long memberId,
            @Param("teamId") Long teamId,
            Pageable pageable);
}
