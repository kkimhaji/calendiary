package com.example.board.domain.role;

import com.example.board.domain.team.Team;
import com.example.board.domain.teamMember.TeamMember;
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
    @Transient
    private String permissions = "0";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @OneToMany(mappedBy = "role")
    private Set<TeamMember> members = new HashSet<>();

    public void setMembers(Set<TeamMember> members){
        this.members = members;
    }

    public boolean hasPermission(TeamPermission permission) {
        return PermissionConverter.hasPermissionOptimized(getPermissionBytes(), permission);
    }

    public void addPermission(TeamPermission permission) {
        String currentPermissions = getPermissions(); // 문자열로 변환
        String newPermissions = PermissionUtils.addPermission(currentPermissions, permission);
        setPermissions(newPermissions); // 문자열을 설정하면 내부적으로 바이트로 변환
    }

    public void setPermissions(Set<TeamPermission> permissions) {
        this.permissionBytes = PermissionConverter.createPermissionBytes(permissions);
        this.permissions = null; // 캐시된 문자열 초기화
    }

    // 권한 문자열 설정 (기존 호환성)
    public void setPermissions(String permissions) {
        this.permissions = permissions;
        this.permissionBytes = PermissionConverter.stringToBytes(permissions);
    }

    // 권한 문자열 가져오기 (기존 호환성)
    public String getPermissions() {
        if (permissions == null && permissionBytes != null) {
            permissions = PermissionConverter.bytesToString(permissionBytes);
        }
        return permissions != null ? permissions : "0";
    }

    // 바이트 배열 직접 설정
    public void setPermissionBytes(byte[] permissionBytes) {
        this.permissionBytes = permissionBytes;
        this.permissions = null; // 캐시된 문자열 초기화
    }

    // 바이트 배열 가져오기
    public byte[] getPermissionBytes() {
        return permissionBytes != null ? permissionBytes : new byte[0];
    }

    // 권한 집합 가져오기
    public Set<TeamPermission> getPermissionSet() {
        return PermissionConverter.getPermissionsFromBytes(getPermissionBytes(), TeamPermission.class);
    }

    public void update(String roleName, String description, String permissions) {
        this.roleName = roleName;
        this.description = description;
        this.permissions = permissions;
    }

    @Builder
    public TeamRole(String roleName, String description, Set<TeamPermission> permissions, Team team) {
        this.roleName = roleName;
        this.description = description;
        this.permissions = PermissionUtils.createPermissionBits(permissions);
        this.team = team;
    }
}
