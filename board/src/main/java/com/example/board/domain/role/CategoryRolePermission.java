package com.example.board.domain.role;

import com.example.board.domain.team.TeamCategory;
import com.example.board.permission.CategoryPermission;
import com.example.board.permission.utils.PermissionConverter;
import com.example.board.permission.utils.PermissionUtils;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryRolePermission {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private TeamCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private TeamRole role;

    @Column(name = "permissions", columnDefinition = "BLOB")
    @Lob
    private byte[] permissionBytes;
    // permissions 문자열 호환을 위한 필드 (마이그레이션 후 제거 가능)
    @Transient
    private String permissions = "0";

    public boolean hasPermission(CategoryPermission permission) {
        return PermissionUtils.hasPermission(this.permissions, permission);
    }

    public static CategoryRolePermission createPermission(
            TeamCategory category,
            TeamRole role,
            Set<CategoryPermission> permissions) {
        CategoryRolePermission permission = new CategoryRolePermission();
        permission.category = category;
        permission.role = role;
        permission.permissions = PermissionUtils.createPermissionBits(permissions);
        return permission;
    }

    public void addPermission(CategoryPermission permission) {
        this.permissions = PermissionUtils.addPermission(this.permissions, permission);
    }

    public String getPermissions() {
        if (permissions == null && permissionBytes != null) {
            // 바이트 배열을 문자열로 변환 (읽기 전용)
            permissions = PermissionConverter.bytesToString(permissionBytes);
        }
        return permissions;
    }

    // 새로운 게터/세터
    public byte[] getPermissionBytes() {
        return permissionBytes;
    }

    public void setPermissionBytes(byte[] permissionBytes) {
        this.permissionBytes = permissionBytes;
        // 문자열 캐시 초기화
        this.permissions = null;
    }

    // 기존 세터는 내부적으로 바이트 배열로 변환
    public void setPermissions(String permissions) {
        this.permissions = permissions;
        this.permissionBytes = PermissionConverter.stringToBytes(permissions);
    }

    public void setPermissions(Set<CategoryPermission> permissions) {
        String permissionBits = "0";
        for (CategoryPermission permission : permissions) {
            permissionBits = PermissionUtils.addPermission(permissionBits, permission);
        }
        this.permissions = permissionBits;
    }

    public void setCategory(TeamCategory category){
        this.category = category;
    }

    public void setRole(TeamRole role){
        this.role = role;
    }

   @Builder
    public CategoryRolePermission(TeamCategory category, TeamRole role, Set<CategoryPermission> permissions) {
        this.category = category;
        this.role = role;
        this.permissions = PermissionUtils.createPermissionBits(permissions);
    }
}