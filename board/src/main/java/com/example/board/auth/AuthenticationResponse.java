package com.example.board.auth;

import jakarta.persistence.Entity;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AuthenticationResponse {
    private String token;
}
