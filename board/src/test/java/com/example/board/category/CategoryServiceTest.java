package com.example.board.category;

import com.example.board.domain.role.TeamRole;
import com.example.board.domain.team.Team;
import com.example.board.domain.category.TeamCategory;
import com.example.board.domain.teamMember.TeamMember;
import com.example.board.service.CategoryService;
import com.example.board.support.AbstractTestSupport;
import com.example.board.support.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

import static com.example.board.permission.CategoryPermission.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ComponentScan("com.example.board")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
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
    void init(){
        team = testDataBuilder.createTeam(member1);
        teamMember = testDataBuilder.addMemberToTeam(member2, team);
        teamRole = testDataBuilder.createNewRole(team, "test role");
        category = testDataBuilder.createCategory(teamRole, team, member1);
    }

    @Test
    void checkCategoryPermissionTest(){
        testDataBuilder.addMemberToRole(member2, teamRole);
        
        assertThat(categoryService.checkCategoryPermission(category.getId(), member1, CREATE_COMMENT)).isTrue();
        assertThat(categoryService.checkCategoryPermission(category.getId(), member2, CREATE_COMMENT)).isFalse();
        assertThat(categoryService.checkCategoryPermission(category.getId(), member2, VIEW_POST)).isTrue();

    }
}
