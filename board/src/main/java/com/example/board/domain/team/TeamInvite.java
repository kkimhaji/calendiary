package com.example.board.domain.team;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamInvite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String code; // UUID 또는 해시값
    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    @Column(name = "max_uses")
    private Integer maxUses = 1;
    @Column(name = "used_count")
    private Integer usedCount = 0;

    // 생성자, 생성 시간 등 추가 필드
    public void incrementUsedCount(){
        if (usedCount >= maxUses) {
            throw new IllegalStateException(
                    "초대 코드 사용 횟수 초과 (최대 " + maxUses + "회)"
            );
        }
        this.usedCount++;
    }

    private TeamInvite(String code, Team team, LocalDateTime expiresAt, Integer maxUses) {
        this.code = code;
        this.team = team;
        this.expiresAt = expiresAt;
        this.maxUses = maxUses;
    }

    public static TeamInvite create(String code, Team team, LocalDateTime expiresAt, Integer maxUses){
        return new TeamInvite(code, team, expiresAt, maxUses);
    }
}

