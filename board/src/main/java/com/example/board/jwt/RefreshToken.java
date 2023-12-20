package com.example.board.jwt;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String rTokenId;
    @Column(nullable = false)
    private Long tokenKey;
    @Column(nullable = false)
    private String token;

    public RefreshToken updateToken(String token){
        this.token = token;
        return this;
    }
}
