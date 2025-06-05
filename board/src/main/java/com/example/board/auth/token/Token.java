package com.example.board.auth.token;

import com.example.board.member.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Token {

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String token;

    @Enumerated(EnumType.STRING)
    private TokenType tokenType = TokenType.BEARER;

    private boolean expired;
    private boolean revoked;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Member member;

    public void setTokenExpired(){
        this.expired = true;
        this.revoked = true;
    }

    private Token(String token, TokenType tokenType, boolean expired, boolean revoked, Member member) {
        this.token = token;
        this.tokenType = tokenType;
        this.expired = expired;
        this.revoked = revoked;
        this.member = member;
    }

    public static Token createToken(String token, TokenType tokenType, boolean expired, boolean revoked, Member member){
        return new Token(token, tokenType, expired, revoked, member);
    }
}

