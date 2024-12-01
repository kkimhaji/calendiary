package com.example.board.domain.post;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("SELECT c FROM Comment c " +
            "LEFT JOIN FETCH c.replies " +
            "JOIN FETCH c.author " +
            "WHERE c.post.id = :postId " +
            "AND c.parent IS NULL " +
            "ORDER BY c.createdAt ASC")
    List<Comment> findAllByPostIdWithReplies(@Param("postId") Long postId);

    @Query("SELECT COUNT(c) FROM Comment c " +
            "WHERE c.post.id = :postId")
    long countByPostId(@Param("postId") Long postId);
}
