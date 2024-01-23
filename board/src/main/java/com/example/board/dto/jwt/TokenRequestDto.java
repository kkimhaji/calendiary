package com.example.board.dto.jwt;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class TokenRequestDto {
    private String accessToken;
    private String refreshToken;
}
