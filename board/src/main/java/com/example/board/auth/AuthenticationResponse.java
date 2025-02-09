package com.example.board.auth;

import jakarta.persistence.Entity;
import lombok.*;

public record AuthenticationResponse (
     String accessToken,
     String refreshToken
){
}
