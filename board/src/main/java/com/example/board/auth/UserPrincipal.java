package com.example.board.auth;

import com.example.board.member.Member;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
public class UserPrincipal implements UserDetails {
    private final Member member;
    private Long testTeamId; // 테스트용 팀 ID 추가

    // 기존 생성자
    public UserPrincipal(Member member) {
        this.member = member;
    }

    // 테스트용 생성자
    public UserPrincipal(Member member, Long testTeamId) {
        this.member = member;
        this.testTeamId = testTeamId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return member.getPassword();
    }

    @Override
    public String getUsername() {
        return member.getEmail();
    }

    // UserDetails의 나머지 메서드 구현
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // 팀 ID setter (테스트용)
    public void setTestTeamId(Long testTeamId) {
        this.testTeamId = testTeamId;
    }

    // 팀 ID getter
    public Long getTestTeamId() {
        return testTeamId;
    }
}
