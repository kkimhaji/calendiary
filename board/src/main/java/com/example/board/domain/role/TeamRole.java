package com.example.board.domain.role;

import com.example.board.domain.team.Team;
import com.example.board.domain.teamMember.TeamMember;
import com.example.board.permission.utils.PermissionUtils;
import com.example.board.permission.TeamPermission;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long id;

    private String roleName;
    private String description;
    private String permissions = "0";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @OneToMany(mappedBy = "role")
    private Set<TeamMember> members = new HashSet<>();

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
        return PermissionUtils.getPermissionsFromBits(this.permissions, TeamPermission.class);
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
