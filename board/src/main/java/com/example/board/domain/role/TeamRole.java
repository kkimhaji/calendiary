package com.example.board.domain.role;

import com.example.board.domain.team.Team;
import com.example.board.domain.teamMember.TeamMember;
import com.example.board.permission.PermissionUtils;
import com.example.board.permission.TeamPermission;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
public class TeamRole {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long id;

    private String roleName;
    private String description;
    private String permissions;

    @CreatedDate
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @OneToMany(mappedBy = "role")
    private Set<TeamMember> members = new HashSet<>();

//    public boolean hasPermission(TeamPermission permission) {
//        return PermissionUtils.hasPermission(this.permissions, permission.getValue());
//    }

    public void addPermission(int position) {
        StringBuilder binary = new StringBuilder(permissions);
        // 길이가 부족하면 확장
        while (binary.length() <= position) {
            binary.insert(0, "0");
        }
        binary.setCharAt(binary.length() - 1 - position, '1');
        this.permissions = binary.toString();
    }

    public boolean hasPermission(int position) {
        if (position >= permissions.length()) {
            return false;
        }
        return permissions.charAt(permissions.length() - 1 - position) == '1';
    }

}
