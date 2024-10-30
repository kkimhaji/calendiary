package com.example.board.domain.jwt;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    @Query(value = """
      select t from Token t inner join Member m\s
      on t.member.memberId = m.memberId\s
      where m.memberId = :id and (t.expired = false or t.revoked = false)\s
      """)
    List<Token> findAllValidTokenByUser(Long id);
    Optional<Token> findByToken(String token);
}
