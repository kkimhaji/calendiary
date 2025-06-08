package com.example.board.role;

import com.example.board.team.Team;
import com.example.board.teamMember.TeamMember;
import com.example.board.permission.utils.PermissionConverter;
import com.example.board.permission.utils.PermissionUtils;
import com.example.board.permission.TeamPermission;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long id;

    private String roleName;
    private String description;

    @Column(name = "permissions", columnDefinition = "BLOB")
    @Lob
    private byte[] permissionBytes = new byte[0]; // 기본값 빈 배열

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @OneToMany(mappedBy = "role")
    private Set<TeamMember> members = new HashSet<>();

    public void setMembers(Set<TeamMember> members){
        this.members = members;
    }

    public boolean hasPermission(TeamPermission permission) {
        return PermissionUtils.hasPermission(this.permissionBytes, permission);
    }

    public void setPermissions(Set<TeamPermission> permissions) {
        this.permissionBytes = PermissionConverter.createPermissionBytes(permissions);
    }

    // 권한 문자열 설정 (기존 호환성)
    public void setPermissions(String permissions) {
        this.permissionBytes = PermissionConverter.stringToBytes(permissions);
    }

    // 바이트 배열 직접 설정
    public void setPermissionBytes(byte[] permissionBytes) {
        this.permissionBytes = permissionBytes;
    }

    // 바이트 배열 가져오기
    public byte[] getPermissionBytes() {
        return permissionBytes != null ? permissionBytes : new byte[0];
    }

    // 권한 집합 가져오기
    public Set<TeamPermission> getPermissionSet() {
        return PermissionConverter.getPermissionsFromBytes(this.permissionBytes, TeamPermission.class);
    }

    public void update(String newRoleName, String description, Set<TeamPermission> newPermissions) {
        // 역할명 업데이트
        if (newRoleName != null && !newRoleName.trim().isEmpty()) {
            this.roleName = newRoleName;
        }
        // 설명 업데이트
        this.description = description;
        // 권한 업데이트 - 엔티티 내부에서 변환 처리
        if (newPermissions != null) {
            this.permissionBytes = PermissionUtils.createPermissionBytes(newPermissions);
        }
    }

    //권한만 업데이트
    public void updatePermissions(Set<TeamPermission> newPermissions) {
        if (newPermissions != null) {
            this.permissionBytes = PermissionUtils.createPermissionBytes(newPermissions);
        }
    }

    //역할&설명만 업데이트
    public void updateBasicInfo(String newRoleName, String description) {
        if (newRoleName != null && !newRoleName.trim().isEmpty()) {
            this.roleName = newRoleName;
        }
        this.description = description;
    }


    private TeamRole(String roleName, String description, Set<TeamPermission> permissions, Team team) {
        if (roleName == null || roleName.trim().isEmpty()) {
            throw new IllegalArgumentException("Role name cannot be null or empty");
        }
        if (team == null) {
            throw new IllegalArgumentException("Team cannot be null");
        }
        if (permissions == null) {
            permissions = new HashSet<>(); // 빈 Set으로 초기화
        }
        this.roleName = roleName;
        this.description = description;
        this.team = team;
        this.permissionBytes = PermissionUtils.createPermissionBytes(permissions);
    }

    private TeamRole(String roleName, String description, byte[] permissionBytes, Team team) {
        if (roleName == null || roleName.trim().isEmpty()) {
            throw new IllegalArgumentException("Role name cannot be null or empty");
        }
        if (team == null) {
            throw new IllegalArgumentException("Team cannot be null");
        }
        this.roleName = roleName;
        this.description = description;
        this.team = team;
        this.permissionBytes = permissionBytes != null ? permissionBytes : new byte[0];
    }

    public static TeamRole create(String roleName, String description,
                                  Set<TeamPermission> permissions, Team team) {
        return new TeamRole(roleName, description, permissions, team);
    }

}
