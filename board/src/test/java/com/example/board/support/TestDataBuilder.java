package com.example.board.support;

import com.example.board.auth.UserPrincipal;
import com.example.board.category.CategoryRepository;
import com.example.board.category.CategoryService;
import com.example.board.category.TeamCategory;
import com.example.board.category.dto.CategoryRolePermissionDTO;
import com.example.board.category.dto.CreateCategoryRequest;
import com.example.board.comment.Comment;
import com.example.board.comment.CommentRepository;
import com.example.board.common.exception.CategoryNotFoundException;
import com.example.board.common.exception.TeamNotFoundException;
import com.example.board.member.Member;
import com.example.board.member.MemberRepository;
import com.example.board.permission.CategoryPermission;
import com.example.board.permission.TeamPermission;
import com.example.board.post.Post;
import com.example.board.post.PostRepository;
import com.example.board.role.TeamRole;
import com.example.board.role.TeamRoleService;
import com.example.board.role.dto.AddMembersToRoleRequest;
import com.example.board.role.dto.CreateRoleRequest;
import com.example.board.team.Team;
import com.example.board.team.TeamRepository;
import com.example.board.team.TeamService;
import com.example.board.team.dto.AddMemberRequestDTO;
import com.example.board.team.dto.TeamCreateRequestDTO;
import com.example.board.teamMember.TeamMember;
import com.example.board.teamMember.TeamMemberRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.example.board.permission.TeamPermission.MANAGE_MEMBERS;
import static com.example.board.permission.TeamPermission.MANAGE_ROLES;

@Component
@RequiredArgsConstructor
@Transactional
public class TestDataBuilder {
    private final TeamService teamService;
    private final TeamRoleService teamRoleService;
    private final CategoryService categoryService;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TeamRepository teamRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final CategoryRepository categoryRepository;
    private final TeamMemberRepository teamMemberRepository;
    @Autowired
    private EntityManager entityManager;

    public Member createMember(String email, String nickname) {
        return memberRepository.save(
                Member.createMember(
                        email, nickname, passwordEncoder.encode("password"), true, null, null
                ));
    }

    public Team createTeam(Member member1) {
        var request = new TeamCreateRequestDTO("testTeam", "test");
        Team team = teamService.createTeam(member1, request);
        entityManager.flush();
        return team;
    }

    public TeamMember addMemberToTeam(Member member2, Long teamId) {
        AddMemberRequestDTO dto = new AddMemberRequestDTO(teamId, member2.getMemberId());
        return teamService.addMember(dto);
    }

    public TeamRole createNewRole(Long teamId, String roleName) {
        Set<TeamPermission> permissions = new HashSet<>(Arrays.asList(
                MANAGE_ROLES, MANAGE_MEMBERS
        ));
        var roleRequest = new CreateRoleRequest(roleName, permissions, "role for test");
        return teamRoleService.createRole(teamId, roleRequest);
    }

    public TeamRole createNewRoleWithPermissions(Team team, String roleName, Set<TeamPermission> permissions) {
        return teamRoleService.createRole(team.getId(), new CreateRoleRequest(roleName, permissions, "new role with permissions"));
    }

    public void addMemberToRole(Member member, TeamRole teamRole) {
        var addRequest = new AddMembersToRoleRequest(teamRole.getId(), Collections.singletonList(member.getMemberId()));
        teamRoleService.addMemberToRole(teamRole.getTeam().getId(), addRequest);
    }

    public TeamCategory createCategory(Long roleId, Long teamId, String categoryName, Set<CategoryPermission> categoryPermissions) {
        return categoryService.createCategory(teamId, forCreateCategoryRequest(teamId, roleId, categoryName, categoryPermissions));
    }

    public TeamCategory createCategory(Long teamId, String categoryName, Set<CategoryPermission> categoryPermissions) {
        Long basicRoleId = teamRepository.findById(teamId).orElseThrow(() -> new TeamNotFoundException("team not found")).getBasicRoleId();
        return categoryService.createCategory(teamId, forCreateCategoryRequest(teamId, basicRoleId, categoryName, categoryPermissions));
    }

    public void updateRolePermission(Long roleId, Set<TeamPermission> permissions) {
        teamRoleService.updateRolePermissions(roleId, permissions);
    }

    public CreateCategoryRequest forCreateCategoryRequest(Long teamId, Long roleId, String categoryName, Set<CategoryPermission> permissions) {
        Team team = teamRepository.findById(teamId).orElseThrow();
        if (roleId == null) {
            roleId = team.getBasicRoleId();
        }
        //admin 설정 - 모든 권한 허용
        CategoryRolePermissionDTO adminDTO = new CategoryRolePermissionDTO(team.getAdminRoleId(), new HashSet<>(Arrays.asList(CategoryPermission.values())));
        CategoryRolePermissionDTO basicDTO = new CategoryRolePermissionDTO(roleId, permissions);
        return new CreateCategoryRequest(categoryName, "category for test", List.of(adminDTO, basicDTO));
    }

    public UserPrincipal getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserPrincipal)) {
            throw new IllegalStateException(
                    "Principal이 UserPrincipal 타입이 아닙니다. 실제 타입: " +
                            principal.getClass().getName());
        }

        return (UserPrincipal) principal;
    }

    public Long getCurrentTestTeamId() {
        return getCurrentUserPrincipal().getTestTeamId();
    }

    public Post createPost(String title, String content, Member author, TeamCategory category, Team team, TeamMember teamMember) {
        return postRepository.save(
                Post.create(title, content, author, category, team, teamMember));
    }

    public Post createPost(String title, String content, Member author, Long categoryId, Long teamId, TeamMember teamMember) {
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new TeamNotFoundException("team not found"));
        TeamCategory category = categoryRepository.findById(categoryId).orElseThrow(() -> new CategoryNotFoundException("category not found"));
        return postRepository.save(Post.create(title, content, author, category, team, teamMember));
    }

    public Post createPost(String title, String content, Long categoryId, Long teamId) {
        Member author = getCurrentUserPrincipal().getMember();
        TeamMember teamMember = teamMemberRepository.findByTeamIdAndMemberId(teamId, author.getMemberId())
                .orElseThrow();
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new TeamNotFoundException("team not found"));
        TeamCategory category = categoryRepository.findById(categoryId).orElseThrow(() -> new CategoryNotFoundException("category not found"));
        return postRepository.save(
                Post.create(title, content, author, category, team, teamMember));
    }

    public Long getCurrentCategoryId() {
        return getCurrentUserPrincipal().getTestCategoryId();
    }

    public Comment createComment(String content, Post post, Member author, TeamMember teamMember) {
        return commentRepository.save(
                Comment.createComment(content, post, author, teamMember, null)
        );
    }

    public TeamMember getTeamMember(Long teamId, Long memberId) {
        return teamMemberRepository.findByTeamIdAndMemberId(teamId, memberId)
                .orElseThrow();
    }

    public TeamRole getAdminRoleByTeam(Team team) {
        return teamRoleService.getRoleById(team.getAdminRoleId());
    }

    public TeamRole getBasicRoleByTeam(Team team) {
        return teamRoleService.getRoleById(team.getBasicRoleId());
    }

    public Team getCurrentTeam() {
        Long teamId = getCurrentTestTeamId();
        return teamRepository.findById(teamId).orElseThrow();
    }

    public TeamCategory getCurrentCategory() {
        Long categoryId = getCurrentCategoryId();
        return categoryRepository.findById(categoryId).orElseThrow();
    }
}