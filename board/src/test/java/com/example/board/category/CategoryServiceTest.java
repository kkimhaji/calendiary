package com.example.board.category;

import com.example.board.category.dto.CategoryRolePermissionDTO;
import com.example.board.category.dto.CategoryRolePermissionResponse;
import com.example.board.category.dto.UpdateCategoryRequest;
import com.example.board.comment.Comment;
import com.example.board.comment.CommentRepository;
import com.example.board.post.Post;
import com.example.board.post.PostRepository;
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
import java.util.List;
import java.util.Optional;

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
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private CommentRepository commentRepository;
    private TeamCategory category;

    @BeforeEach
    void init() {
        team = testDataBuilder.createTeam(member1);
        teamMember = testDataBuilder.addMemberToTeam(member2, team.getId());
        teamRole = testDataBuilder.createNewRole(team.getId(), "test role");
        category = testDataBuilder.createCategory(teamRole.getId(), team.getId(), "test category", new HashSet<>(Arrays.asList(VIEW_POST, DELETE_POST)));
    }

    @Test
    void checkCategoryPermissionTest() {
        testDataBuilder.addMemberToRole(member2, teamRole);

        assertThat(categoryService.checkCategoryPermission(category.getId(), member1, CREATE_COMMENT)).isTrue();
        assertThat(categoryService.checkCategoryPermission(category.getId(), member2, CREATE_COMMENT)).isFalse();
        assertThat(categoryService.checkCategoryPermission(category.getId(), member2, VIEW_POST)).isTrue();
    }

    @Test
    void updateCategoryTest() {
        String updateName = "updated category";
        String updateDesc = "update category test";
        var dto = new CategoryRolePermissionDTO(teamRole.getId(), new HashSet<>(List.of(CREATE_POST)));
        var request = new UpdateCategoryRequest(updateName, updateDesc, Optional.of(List.of(dto)));
        var updatedCategory = categoryService.updateCategory(team.getId(), category.getId(), request);

        assertThat(updatedCategory.name()).isEqualTo(updateName);
        assertThat(updatedCategory.description()).isEqualTo(updateDesc);
        assertThat(updatedCategory.id()).isEqualTo(category.getId());
        assertThat(updatedCategory.rolePermissions()).hasSize(1);

        CategoryRolePermissionResponse rolePermission = updatedCategory.rolePermissions().get(0);

        assertThat(rolePermission.roleId()).isEqualTo(teamRole.getId());
        assertThat(rolePermission.roleName()).isEqualTo(teamRole.getRoleName());
        assertThat(rolePermission.permissions()).contains(CREATE_POST);
    }

    @Test
    void deleteCategoryTest(){
        Long categoryId = category.getId();
        Post post = testDataBuilder.createPost("test post", "test", member2, category, team, teamMember);
        Comment comment = testDataBuilder.createComment("test comment", post, member2, teamMember);
        Long commentId = comment.getId();
        categoryService.deleteCategory(categoryId);

        assertThat(categoryRepository.findById(categoryId)).isEmpty();
        assertThat(commentRepository.findById(commentId)).isEmpty();
        assertThat(postRepository.findAllByCategory(category)).isEmpty();
    }
}
