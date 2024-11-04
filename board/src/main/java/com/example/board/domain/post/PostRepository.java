package com.example.board.domain.post;

import com.example.board.domain.member.Member;
import com.example.board.domain.team.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByAuthor(Member member);
    List<Post> findAllByTeam(Team team);
}