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
        return PermissionUtils.hasPermission(this.permissionBytes, permission);
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
//            this.permissions = PermissionUtils.createPermissionBits(newPermissions); // 호환성
        }
    }

    //권한만 업데이트
    public void updatePermissions(Set<TeamPermission> newPermissions) {
        if (newPermissions != null) {
            this.permissionBytes = PermissionUtils.createPermissionBytes(newPermissions);
//            this.permissions = PermissionUtils.createPermissionBits(newPermissions);
        }
    }

    //역할&설명만 업데이트
    public void updateBasicInfo(String newRoleName, String description) {
        if (newRoleName != null && !newRoleName.trim().isEmpty()) {
            this.roleName = newRoleName;
        }
        this.description = description;
    }

    @PostLoad
    private void syncPermissionsAfterLoad() {
        // DB에서 로딩 후 byte[]를 기반으로 String permissions 동기화
        if (this.permissionBytes != null && this.permissionBytes.length > 0) {
            Set<TeamPermission> permissionSet = PermissionUtils.getPermissionsFromBytes(
                    this.permissionBytes, TeamPermission.class);
            this.permissions = PermissionUtils.createPermissionBits(permissionSet);
        } else {
            this.permissions = "0";
        }
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

        // 호환성을 위한 String 생성 (byte[]에서 역변환)
        Set<TeamPermission> permissionSet = PermissionUtils.getPermissionsFromBytes(
                this.permissionBytes, TeamPermission.class);
        this.permissions = PermissionUtils.createPermissionBits(permissionSet);
    }

    public static TeamRole create(String roleName, String description,
                                  Set<TeamPermission> permissions, Team team) {
        return new TeamRole(roleName, description, permissions, team);
    }

}
