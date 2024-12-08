package com.example.board.domain.role;

import com.example.board.domain.team.Team;
import com.example.board.domain.team.TeamCategory;
import com.example.board.permission.PermissionUtils;
import com.example.board.permission.TeamPermission;
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

    private String permissions = "0";

    public boolean hasPermission(TeamPermission permission) {
        return PermissionUtils.hasPermission(this.permissions, permission);
    }

    public static CategoryRolePermission createPermission(
            TeamCategory category,
            TeamRole role,
            Set<TeamPermission> permissions) {
        CategoryRolePermission permission = new CategoryRolePermission();
        permission.category = category;
        permission.role = role;
        permission.permissions = PermissionUtils.createPermissionBits(permissions);
        return permission;
    }

    public void addPermission(TeamPermission permission) {
        this.permissions = PermissionUtils.addPermission(this.permissions, permission);
    }

    public void setPermissions(Set<TeamPermission> permissions) {
        String permissionBits = "0";
        for (TeamPermission permission : permissions) {
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
    public CategoryRolePermission(TeamCategory category, TeamRole role, Set<TeamPermission> permissions) {
        this.category = category;
        this.role = role;
        this.permissions = PermissionUtils.createPermissionBits(permissions);
    }
}
