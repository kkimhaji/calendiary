package com.example.board.domain.jwt;

import com.example.board.domain.member.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Token {

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String token;

    @Enumerated(EnumType.STRING)
    private TokenType tokenType = TokenType.BEARER;

    private boolean expired;

    //in case you want to revoke manually
    //or e.g. if you want to implement a mechanism when restart application/server - you want to revoke all the tokens
    //flags for it
    private boolean revoked;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Member member;
}

