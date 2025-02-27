package com.example.board.domain.member;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private String nickname;
    @Column(nullable = false)
    private String password;

    private boolean enabled;
    private String verificationCode;
    private LocalDateTime verificationCodeExpiredAt;

//    @OneToMany(mappedBy = "member")
//    @JsonIgnore
//    private Set<TeamMember> teamMemberships = new HashSet<>();


//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        Set<GrantedAuthority> authorities = new HashSet<>();
//
//        // 각 팀별 역할에 따른 권한 추가
//        teamMemberships.forEach(membership -> {
//            // 팀별 역할 권한
//            authorities.add(new SimpleGrantedAuthority(
//                    String.format("TEAM_%d_ROLE_%s",
//                            membership.getTeam().getTeamId(),
//                            membership.getRole().getRoleName())
//            ));
//
//            // 팀별 상세 권한
//            String permissions = membership.getRole().getPermissions();
//            for (TeamPermission permission : TeamPermission.values()) {
//                if (PermissionUtils.hasPermission(permissions, permission)) {
//                    authorities.add(new SimpleGrantedAuthority(
//                            String.format("TEAM_%d_PERMISSION_%s",
//                                    membership.getTeam().getTeamId(),
//                                    permission.name())
//                    ));
//                }
//            }
//        });
//
//        return authorities;
//    }
//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        return new ArrayList<>();
//    }
//
//    @Override
//    public String getUsername() {
//        return email;
//    }
//
//    @Override
//    public boolean isAccountNonExpired() {
//        return true;
//    }
//
//    @Override
//    public boolean isAccountNonLocked() {
//        return true;
//    }
//
//    @Override
//    public boolean isCredentialsNonExpired() {
//        return true;
//    }
//
//    @Override
//    public boolean isEnabled() {
//        return true;
//    }


    public void setVerified(){
        this.enabled = true;
        this.verificationCode = null;
        this.verificationCodeExpiredAt = null;
    }

    public void setVerification(String code, LocalDateTime expiredAt){
        this.verificationCode = code;
        this.verificationCodeExpiredAt = expiredAt;
    }


    public void updateName(String newNickname) {
        this.nickname = newNickname;
    }

    public void updatePassword(String newPassword)
    {
        this.password = newPassword;
    }
}
