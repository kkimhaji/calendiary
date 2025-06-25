package com.example.board.category;

import com.example.board.role.TeamRole;
import com.example.board.support.AbstractTestSupport;
import com.example.board.support.TestDataBuilder;
import com.example.board.team.Team;
import com.example.board.teamMember.TeamMember;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.HashSet;

import static com.example.board.permission.CategoryPermission.*;
import static org.assertj.core.api.Assertions.assertThat;

public class CategoryServiceTest extends AbstractTestSupport {

    @Autowired
    private CategoryService categoryService;
    private Team team;
    private TeamRole teamRole;
    private TeamMember teamMember;
    @Autowired
    private TestDataBuilder testDataBuilder;
    private TeamCategory category;

    @BeforeEach
    void init() {
        team = testDataBuilder.createTeam(member1);
        teamMember = testDataBuilder.addMemberToTeam(member2, team);
        teamRole = testDataBuilder.createNewRole(team, "test role");
        category = testDataBuilder.createCategory(teamRole.getId(), team, member1,new HashSet<>(Arrays.asList(VIEW_POST, DELETE_POST)));
    }

    @Test
    void checkCategoryPermissionTest() {
        testDataBuilder.addMemberToRole(member2, teamRole);

        assertThat(categoryService.checkCategoryPermission(category.getId(), member1, CREATE_COMMENT)).isTrue();
        assertThat(categoryService.checkCategoryPermission(category.getId(), member2, CREATE_COMMENT)).isFalse();
        assertThat(categoryService.checkCategoryPermission(category.getId(), member2, VIEW_POST)).isTrue();

    }
}
