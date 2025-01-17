package com.example.board.controller;

import com.example.board.auth.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Slf4j
public class TestController {

    @GetMapping("/test")
    public ResponseEntity<String> testConnection(){
        return ResponseEntity.ok("서버 연결 성공");
    }

    @GetMapping("/preauth/test/{teamId}")
    @PreAuthorize("hasPermission(#teamId, 'Team', T(com.example.board.permission.TeamPermission).MANAGE_CATEGORIES)")
    public ResponseEntity<String> preAuthorizeTest(@PathVariable(name = "teamId") @P("teamId") Long teamId, @AuthenticationPrincipal UserPrincipal userPrincipal){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.debug("Current Authentication in Controller: {}", auth);
        log.debug("Principal in Controller: {}", auth != null ? auth.getPrincipal() : "null");
        return ResponseEntity.ok("카테고리 관리 권한 인증 성공");
    }
}
