package com.example.board.role;

import com.example.board.category.TeamCategory;
import com.example.board.permission.CategoryPermission;
import com.example.board.permission.utils.PermissionConverter;
import com.example.board.permission.utils.PermissionUtils;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
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

    public boolean hasPermission(CategoryPermission permission) {
        return PermissionUtils.hasPermission(this.permissionBytes, permission);
    }

    private CategoryRolePermission(TeamCategory category, TeamRole role, Set<CategoryPermission> permissions) {
        this.category = category;
        this.role = role;
        this.permissionBytes = PermissionUtils.createPermissionBytes(permissions);
    }

    public static CategoryRolePermission create(
            TeamCategory category,
            TeamRole role,
            Set<CategoryPermission> permissions) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        if (permissions == null) {
            permissions = new HashSet<>(); // 빈 권한 집합으로 초기화
        }
        CategoryRolePermission permission = new CategoryRolePermission(category, role, permissions);
        category.getRolePermissions().add(permission);
        return new CategoryRolePermission(category, role, permissions);
    }

    public byte[] getPermissionBytes() {
        return permissionBytes;
    }

    public void setPermissionBytes(byte[] permissionBytes) {
        this.permissionBytes = permissionBytes;
    }

    // 기존 세터는 내부적으로 바이트 배열로 변환
    public void setPermissions(String permissions) {
        this.permissionBytes = PermissionConverter.stringToBytes(permissions);
    }

    public void setCategory(TeamCategory category){
        this.category = category;
    }

    public void setRole(TeamRole role){
        this.role = role;
    }

}