package com.example.board.category;

import com.example.board.category.dto.*;
import com.example.board.comment.Comment;
import com.example.board.comment.CommentRepository;
import com.example.board.member.Member;
import com.example.board.permission.CategoryPermission;
import com.example.board.post.Post;
import com.example.board.post.PostRepository;
import com.example.board.role.CategoryPermissionRepository;
import com.example.board.role.TeamRole;
import com.example.board.support.AbstractTestSupport;
import com.example.board.support.TestDataBuilder;
import com.example.board.team.Team;
import com.example.board.teamMember.TeamMember;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.example.board.permission.CategoryPermission.CREATE_POST;
import static com.example.board.permission.CategoryPermission.DELETE_POST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CategoryServiceTest extends AbstractTestSupport {

    @Autowired
    private CategoryService categoryService;
    private Team team;
    private TeamRole teamRole;
    @Autowired
    private TestDataBuilder testDataBuilder;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private CommentRepository commentRepository;
    private TeamCategory category;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private CategoryPermissionRepository categoryPermissionRepository;

    @BeforeEach
    void init() {
        team = testDataBuilder.createTeam(member1);
        teamRole = testDataBuilder.createNewRole(team.getId(), "test role");
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("카테고리 생성 정상")
    void createCategory_success() {
        // given

        CreateCategoryRequest req = new CreateCategoryRequest(
                "공지", "공지를 위한 카테고리",
                List.of(new CategoryRolePermissionDTO(team.getAdminRoleId(), Set.of(CREATE_POST)),
                        new CategoryRolePermissionDTO(team.getBasicRoleId(), Set.of(CategoryPermission.DELETE_POST)))
        );
        // when
        TeamCategory category = categoryService.createCategory(team.getId(), req);

        entityManager.flush();
        entityManager.clear();

        TeamCategory persistedCategory = categoryRepository.findById(category.getId()).orElseThrow();
        // then
        assertThat(persistedCategory.getName()).isEqualTo("공지");
        assertThat(persistedCategory.getDescription()).isEqualTo("공지를 위한 카테고리");
        // 권한 부여된 역할 검증

        assertThat(persistedCategory.getRolePermissions()).hasSize(2);
        assertThat(persistedCategory.getRolePermissions()).anyMatch(
                crp -> crp.getRole().getId().equals(team.getAdminRoleId())
                        && crp.hasPermission(CREATE_POST)
        );
    }

    @Test
    @DisplayName("카테고리 생성 실패 - 같은 이름 이미 존재")
    void createCategory_duplicateName_fail() {
        categoryService.createCategory(team.getId(),
                new CreateCategoryRequest("중복", "desc",
                        List.of(new CategoryRolePermissionDTO(team.getAdminRoleId(), Set.of()))));

        CreateCategoryRequest req2 = new CreateCategoryRequest("중복", "desc2",
                List.of(new CategoryRolePermissionDTO(team.getAdminRoleId(), Set.of())));
        assertThatThrownBy(() -> categoryService.createCategory(team.getId(), req2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("카테고리 생성 실패 - 유효하지 않은 roleId")
    void createCategory_invalidRoleId_fail() {
        Long invalidRoleId = 999999L;
        CreateCategoryRequest req = new CreateCategoryRequest("공지2", "desc",
                List.of(new CategoryRolePermissionDTO(invalidRoleId, Set.of(CREATE_POST))));
        assertThatThrownBy(() -> categoryService.createCategory(team.getId(), req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid role IDs:");
    }

    @Test
    @DisplayName("카테고리 권한 체크: 부여된 권한만 true")
    void checkCategoryPermission_success() {
        Member member = testDataBuilder.createMember("a@b.com", "mb1");
        // 역할1 소속으로 팀멤버 등록, 카테고리 생성
        testDataBuilder.addMemberToTeam(member, team.getId());
        testDataBuilder.addMemberToRole(member, teamRole);

        CreateCategoryRequest req = new CreateCategoryRequest(
                "공지", "desc",
                List.of(new CategoryRolePermissionDTO(teamRole.getId(), Set.of(DELETE_POST)))
        );
        TeamCategory category = categoryService.createCategory(team.getId(), req);

        // when & then
        assertThat(categoryService.checkCategoryPermission(category.getId(), member, DELETE_POST)).isTrue();
        assertThat(categoryService.checkCategoryPermission(category.getId(), member, CREATE_POST)).isFalse();
    }

    @Test
    @DisplayName("카테고리 정보 수정 - 이름, 설명 및 권한 변경")
    void updateCategory_success() {
        // given: 기존 카테고리
        TeamCategory category = categoryService.createCategory(team.getId(),
                new CreateCategoryRequest("원래이름", "old", List.of(new CategoryRolePermissionDTO(team.getAdminRoleId(), Set.of()))));

        // when
        UpdateCategoryRequest req = new UpdateCategoryRequest(
                "업데이트이름", "새설명",
                Optional.of(List.of(
                        new CategoryRolePermissionDTO(team.getAdminRoleId(), Set.of(CREATE_POST)),
                        new CategoryRolePermissionDTO(team.getBasicRoleId(), Set.of(DELETE_POST)))
                )
        );

        CategoryResponse resp = categoryService.updateCategory(team.getId(), category.getId(), req);

        // then
        assertThat(resp.name()).isEqualTo("업데이트이름");
        assertThat(resp.description()).isEqualTo("새설명");
        assertThat(resp.rolePermissions())
                .anySatisfy(dto ->
                        assertThat(dto.roleId()).isIn(team.getAdminRoleId(), team.getBasicRoleId()));
    }

    @Test
    @DisplayName("카테고리 정보 수정: 중복이름시 예외")
    void updateCategory_duplicateName_fail() {
        TeamCategory c1 = categoryService.createCategory(team.getId(),
                new CreateCategoryRequest("이름1", "desc", List.of(new CategoryRolePermissionDTO(team.getAdminRoleId(), Set.of()))));
        TeamCategory c2 = categoryService.createCategory(team.getId(),
                new CreateCategoryRequest("이름2", "desc2", List.of(new CategoryRolePermissionDTO(team.getAdminRoleId(), Set.of()))));

        UpdateCategoryRequest req = new UpdateCategoryRequest("이름1", "upd", Optional.empty());
        assertThatThrownBy(() -> categoryService.updateCategory(team.getId(), c2.getId(), req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("카테고리 삭제: 포스트, 권한, 댓글 DB에서 모두 삭제됨")
    void deleteCategory_deletesAllAssociations() {
        // given: 포스트/댓글/권한 생성
        TeamCategory category = categoryService.createCategory(team.getId(),
                new CreateCategoryRequest("공지", "desc", List.of(new CategoryRolePermissionDTO(team.getAdminRoleId(), Set.of(CREATE_POST)))));
        Member member = testDataBuilder.createMember("xx@yy.com", "mm");

        TeamMember tm = testDataBuilder.addMemberToTeam(member, team.getId());

        Post post = testDataBuilder.createPost("title", "content", member, category, team, tm);
        Comment comment = testDataBuilder.createComment("댓글", post, member, tm);

        // when
        categoryService.deleteCategory(category.getId());

        // then
        assertThat(postRepository.findById(post.getId())).isEmpty();
        assertThat(commentRepository.findById(comment.getId())).isEmpty();
        assertThat(categoryRepository.findById(category.getId())).isEmpty();
        assertThat(categoryPermissionRepository.findAllByCategory(category)).isEmpty();
    }

    @Test
    @DisplayName("팀 내 모든 카테고리 삭제 (연쇄 삭제)")
    void deleteAllCategoriesInTeam_success() {
        TeamCategory c1 = categoryService.createCategory(team.getId(),
                new CreateCategoryRequest("cat1", "d1", List.of(new CategoryRolePermissionDTO(team.getAdminRoleId(), Set.of()))));
        TeamCategory c2 = categoryService.createCategory(team.getId(),
                new CreateCategoryRequest("cat2", "d2", List.of(new CategoryRolePermissionDTO(team.getBasicRoleId(), Set.of()))));
        categoryService.deleteAllCategoriesInTeam(team);

        assertThat(categoryRepository.findAllByTeam(team)).isEmpty();
    }

    @Test
    @DisplayName("팀으로 카테고리 리스트 조회")
    void getCategoryListByTeam_success() {
        categoryService.createCategory(team.getId(),
                new CreateCategoryRequest("cat1", "d1", List.of(new CategoryRolePermissionDTO(team.getAdminRoleId(), Set.of()))));
        categoryService.createCategory(team.getId(),
                new CreateCategoryRequest("cat2", "d2", List.of(new CategoryRolePermissionDTO(team.getBasicRoleId(), Set.of()))));

        List<CategoryListDTO> list = categoryService.getCategoryListByTeam(team.getId());
        assertThat(list).hasSize(2);
        assertThat(list).extracting(CategoryListDTO::name).contains("cat1", "cat2");
    }

    @Test
    @DisplayName("카테고리 단건 조회 정상")
    void getCategoryInfo_success() {
        TeamCategory c = categoryService.createCategory(team.getId(),
                new CreateCategoryRequest("forinfo", "desc", List.of(new CategoryRolePermissionDTO(team.getAdminRoleId(), Set.of()))));
        CategoryResponse resp = categoryService.getCategoryInfo(c.getId());
        assertThat(resp.name()).isEqualTo("forinfo");
        assertThat(resp.description()).isEqualTo("desc");
    }
}
