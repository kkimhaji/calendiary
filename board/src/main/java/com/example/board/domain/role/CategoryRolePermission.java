package com.example.board.domain.role;

import com.example.board.domain.team.TeamCategory;
import com.example.board.permission.PermissionUtils;
import com.example.board.permission.TeamPermission;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Getter
@Setter
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

   @Builder
    public CategoryRolePermission(TeamCategory category, TeamRole role, String permissions) {
        this.category = category;
        this.role = role;
        this.permissions = permissions;
    }
}
