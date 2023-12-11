package com.example.board.domain.post;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Long, Post> {
    @Override
    List<Long> findAllById(Iterable<Post> posts);

    List<Post> findByMemberId(Long Member);
}
