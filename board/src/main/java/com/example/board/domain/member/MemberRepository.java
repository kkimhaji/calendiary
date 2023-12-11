package com.example.board.domain.member;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Long, Member> {
    @Override
    Optional<Long> findById(Member member);
}
