package com.example.board.domain.team;

import com.example.board.domain.member.Member;
import com.example.board.domain.teamMember.TeamMember;
import com.example.board.permission.PermissionUtils;
import com.example.board.permission.TeamPermission;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
public class TeamRole {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long id;

    private String roleName;
    private String description;
    private String permissions = "0";

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

//    public void addPermission(int position) {
//        StringBuilder binary = new StringBuilder(permissions);
//        // 길이가 부족하면 확장
//        while (binary.length() <= position) {
//            binary.insert(0, "0");
//        }
//        binary.setCharAt(binary.length() - 1 - position, '1');
//        this.permissions = binary.toString();
//    }
//
//    public boolean hasPermission(int position) {
//        if (position >= permissions.length()) {
//            return false;
//        }
//        return permissions.charAt(permissions.length() - 1 - position) == '1';
//    }
public boolean hasPermission(TeamPermission permission) {
    return PermissionUtils.hasPermission(this.permissions, permission);
}

    public void addPermission(TeamPermission permission) {
        this.permissions = PermissionUtils.addPermission(this.permissions, permission);
    }

    public void setPermissions(Set<TeamPermission> permissions) {
        this.permissions = PermissionUtils.createPermissionBits(permissions);
    }

    public Set<TeamPermission> getPermissionSet() {
        return PermissionUtils.getPermissionsFromBits(this.permissions);
    }

    public void setAdmin(Team team){
        this.permissions = "11111";
        this.createdAt = LocalDateTime.now();
        this.roleName = "CREATOR";
        this.description = "Who made this Team";
        this.setTeam(team);
    }
}
