package com.example.board.teamMember;

import com.example.board.auth.UserPrincipal;
import com.example.board.category.TeamCategory;
import com.example.board.comment.Comment;
import com.example.board.comment.CommentRepository;
import com.example.board.config.security.WithMockTeamPermission;
import com.example.board.member.Member;
import com.example.board.post.Post;
import com.example.board.post.PostRepository;
import com.example.board.support.AbstractControllerTestSupport;
import com.example.board.support.TestDataBuilder;
import com.example.board.team.Team;
import com.example.board.teamMember.dto.ChangeTeamNicknameRequest;
import com.example.board.teamMember.dto.RemoveMemberRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class TeamMemberControllerTest extends AbstractControllerTestSupport {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TestDataBuilder testDataBuilder;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TeamMemberRepository teamMemberRepository;
    @MockBean
    private PostRepository postRepository;
    @MockBean
    private CommentRepository commentRepository;

    private Team testTeam;
    private Member testMember;
    private TeamMember testTeamMember;
    private String jwtToken;

    @BeforeEach
    void init() {
        testTeam = testDataBuilder.createTeam(member1);
        testTeamMember = testDataBuilder.addMemberToTeam(member2, testTeam.getId());
    }

    @Test
    @WithMockTeamPermission
    @DisplayName("팀 멤버 목록 조회 성공")
    void getTeamMembers_success() throws Exception {
        // given
        UserPrincipal userPrincipal = testDataBuilder.getCurrentUserPrincipal();

        // when & then
        mockMvc.perform(get("/team/{teamId}/members", testTeam.getId())
                        .with(user(userPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].teamNickname").exists())
                .andExpect(jsonPath("$[0].email").exists())
                .andExpect(jsonPath("$[0].roleName").exists());
    }

    @Test
    @DisplayName("팀 멤버 목록 조회 실패 - 인증되지 않은 사용자")
    void getTeamMembers_unauthenticated_unauthorized() throws Exception {
        // when & then
        mockMvc.perform(get("/team/{teamId}/members", testTeam.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockTeamPermission
    @DisplayName("팀 멤버 목록 조회 실패 - 존재하지 않는 팀")
    void getTeamMembers_teamNotFound_notFound() throws Exception {
        // given
        UserPrincipal userPrincipal = testDataBuilder.getCurrentUserPrincipal();
        Long nonExistentTeamId = 999L;

        // when & then
        mockMvc.perform(get("/team/{teamId}/members", nonExistentTeamId)
                        .with(user(userPrincipal)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("team not found"));
    }

    @Test
    @WithMockTeamPermission
    @DisplayName("검색 조건으로 팀 멤버 조회 성공")
    void getTeamMembersWithSearch_success() throws Exception {
        // given
        UserPrincipal userPrincipal = testDataBuilder.getCurrentUserPrincipal();

        // when & then
        mockMvc.perform(get("/team/{teamId}/get-members", testTeam.getId())
                        .param("page", "0")
                        .param("size", "10")
                        .param("keyword", "test")
                        .with(user(userPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").exists())
                .andExpect(jsonPath("$.content[0].memberId").exists())
                .andExpect(jsonPath("$.content[0].email").exists())
                .andExpect(jsonPath("$.content[0].teamNickname").exists())
                .andExpect(jsonPath("$.content[0].roleName").exists())
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.totalPages").exists())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @WithMockTeamPermission
    @DisplayName("검색 조건으로 팀 멤버 조회 성공 - 빈 키워드")
    void getTeamMembersWithSearch_emptyKeyword_success() throws Exception {
        // given
        UserPrincipal userPrincipal = testDataBuilder.getCurrentUserPrincipal();

        // when & then
        mockMvc.perform(get("/team/{teamId}/get-members", testTeam.getId())
                        .param("page", "0")
                        .param("size", "10")
                        .param("keyword", "")
                        .with(user(userPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(greaterThan(0)));
    }

    @Test
    @DisplayName("검색 조건으로 팀 멤버 조회 실패 - 인증되지 않은 사용자")
    void getTeamMembersWithSearch_unauthenticated_unauthorized() throws Exception {
        // when & then
        mockMvc.perform(get("/team/{teamId}/get-members", testTeam.getId())
                        .param("page", "0")
                        .param("size", "10")
                        .param("keyword", "test"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockTeamPermission
    @DisplayName("팀 닉네임 업데이트 성공")
    void updateTeamNickname_success() throws Exception {
        // given
        UserPrincipal userPrincipal = testDataBuilder.getCurrentUserPrincipal();
        String newNickname = "newTestNickname";
        ChangeTeamNicknameRequest request = new ChangeTeamNicknameRequest(newNickname);

        // when & then
        mockMvc.perform(put("/team/{teamId}/nickname", userPrincipal.getTestTeamId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(userPrincipal)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(newNickname)));

        // 실제 DB에서 변경되었는지 확인
        TeamMember updatedTeamMember = teamMemberRepository.findByTeamIdAndMember(
                userPrincipal.getTestTeamId(), userPrincipal.getMember()).orElseThrow();
        assertThat(updatedTeamMember.getTeamNickname()).isEqualTo(newNickname);
    }

    @Test
    @DisplayName("팀 닉네임 업데이트 실패 - 인증되지 않은 사용자")
    void updateTeamNickname_unauthenticated_unauthorized() throws Exception {
        // given
        ChangeTeamNicknameRequest request = new ChangeTeamNicknameRequest("newNickname");

        // when & then
        mockMvc.perform(put("/team/{teamId}/nickname", testTeam.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockTeamPermission
    @DisplayName("팀 닉네임 업데이트 실패 - 존재하지 않는 팀")
    void updateTeamNickname_teamNotFound_notFound() throws Exception {
        // given
        UserPrincipal userPrincipal = testDataBuilder.getCurrentUserPrincipal();
        Long nonExistentTeamId = 999L;
        ChangeTeamNicknameRequest request = new ChangeTeamNicknameRequest("newNickname");

        // when & then
        mockMvc.perform(put("/team/{teamId}/nickname", nonExistentTeamId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(userPrincipal)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("team not found"));
    }

    @Test
    @WithMockTeamPermission
    @DisplayName("팀 닉네임 업데이트 실패 - 팀 멤버가 아님")
    void updateTeamNickname_teamMemberNotFound_notFound() throws Exception {
        // given
        UserPrincipal userPrincipal = testDataBuilder.getCurrentUserPrincipal();
        ChangeTeamNicknameRequest request = new ChangeTeamNicknameRequest("newNickname");

        // when & then
        mockMvc.perform(put("/team/{teamId}/nickname", testTeam.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(userPrincipal)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("cannot find team member"));
    }

    @Test
    @WithMockTeamPermission
    @DisplayName("팀 닉네임 중복 체크 성공 - 중복되지 않음")
    void checkTeamNicknameDuplicate_notDuplicate_success() throws Exception {
        // given
        UserPrincipal userPrincipal = testDataBuilder.getCurrentUserPrincipal();
        String uniqueNickname = "uniqueNickname";

        // when & then
        mockMvc.perform(get("/team/{teamId}/nickname/check", userPrincipal.getTestTeamId())
                        .param("teamNickname", uniqueNickname)
                        .with(user(userPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isDuplicate").value(false))
                .andExpect(jsonPath("$.message").doesNotExist());
    }

    @Test
    @WithMockTeamPermission
    @DisplayName("팀 닉네임 중복 체크 성공 - 중복됨")
    void checkTeamNicknameDuplicate_duplicate_success() throws Exception {
        // given
        UserPrincipal userPrincipal = testDataBuilder.getCurrentUserPrincipal();
        String duplicateNickname = testTeamMember.getTeamNickname();

        // when & then
        mockMvc.perform(get("/team/{teamId}/nickname/check", testTeam.getId())
                        .param("teamNickname", duplicateNickname)
                        .with(user(userPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isDuplicate").value(true))
                .andExpect(jsonPath("$.message").doesNotExist());
    }

    @Test
    @WithMockTeamPermission
    @DisplayName("팀 닉네임 중복 체크 실패 - 존재하지 않는 팀")
    void checkTeamNicknameDuplicate_teamNotFound_notFound() throws Exception {
        // given
        UserPrincipal userPrincipal = testDataBuilder.getCurrentUserPrincipal();
        Long nonExistentTeamId = 999L;
        String nickname = "anyNickname";

        // when & then
        mockMvc.perform(get("/team/{teamId}/nickname/check", nonExistentTeamId)
                        .param("teamNickname", nickname)
                        .with(user(userPrincipal)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("team not found"));
    }

    @Test
    @DisplayName("팀 닉네임 중복 체크 실패 - 인증되지 않은 사용자")
    void checkTeamNicknameDuplicate_unauthenticated_unauthorized() throws Exception {
        // when & then
        mockMvc.perform(get("/team/{teamId}/nickname/check", testTeam.getId())
                        .param("teamNickname", "anyNickname"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockTeamPermission
    @DisplayName("팀 닉네임 중복 체크 실패 - 잘못된 요청 파라미터")
    void checkTeamNicknameDuplicate_invalidParameter_badRequest() throws Exception {
        // given
        UserPrincipal userPrincipal = testDataBuilder.getCurrentUserPrincipal();

        // when & then
        mockMvc.perform(get("/team/{teamId}/nickname/check", userPrincipal.getTestTeamId())
                        .param("teamNickname", "")
                        .with(user(userPrincipal)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockTeamPermission
    @DisplayName("페이징 파라미터 테스트")
    void getTeamMembersWithSearch_pagination_success() throws Exception {
        // given
        UserPrincipal userPrincipal = testDataBuilder.getCurrentUserPrincipal();
        Long teamId = userPrincipal.getTestTeamId();
        // 추가 멤버들 생성
        for (int i = 0; i < 15; i++) {
            Member additionalMember = testDataBuilder.createMember("test" + i + "@example.com", "testUser" + i);
            testDataBuilder.addMemberToTeam(additionalMember, teamId);
        }

        // when & then
        mockMvc.perform(get("/team/{teamId}/get-members", teamId)
                        .param("page", "0")
                        .param("size", "10")
                        .param("keyword", "")
                        .with(user(userPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.totalElements").value(17)) // 기존 2개 + 추가 15개
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(10));
    }

    @Test
    @WithMockTeamPermission
    @DisplayName("두 번째 페이지 조회 테스트")
    void getTeamMembersWithSearch_secondPage_success() throws Exception {
        // given
        UserPrincipal userPrincipal = testDataBuilder.getCurrentUserPrincipal();
        Long teamId = userPrincipal.getTestTeamId();
        // 추가 멤버들 생성
        for (int i = 0; i < 15; i++) {
            Member additionalMember = testDataBuilder.createMember("test" + i + "@example.com", "testUser" + i);
            testDataBuilder.addMemberToTeam(additionalMember, teamId);
        }

        // when & then
        mockMvc.perform(get("/team/{teamId}/get-members", teamId)
                        .param("page", "1")
                        .param("size", "10")
                        .param("keyword", "")
                        .with(user(userPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.totalElements").value(17))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.currentPage").value(1))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(7)); // 두 번째 페이지에는 7개
    }

    @Test
    @WithMockTeamPermission
    @DisplayName("팀 닉네임 업데이트 실패 - 빈 닉네임")
    void updateTeamNickname_emptyNickname_badRequest() throws Exception {
        // given
        UserPrincipal userPrincipal = testDataBuilder.getCurrentUserPrincipal();
        ChangeTeamNicknameRequest request = new ChangeTeamNicknameRequest("");

        // when & then
        mockMvc.perform(put("/team/{teamId}/nickname", userPrincipal.getTestTeamId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(userPrincipal)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").exists());
    }


    @Test
    @WithMockTeamPermission(teamPermissions = {"MANAGE_MEMBERS"})
    @DisplayName("팀 멤버 강제 탈퇴 성공 - 게시글/댓글 삭제")
    void removeMember_deleteContent_success() throws Exception {
        // given
        UserPrincipal userPrincipal = testDataBuilder.getCurrentUserPrincipal();
        TeamCategory category = testDataBuilder.createCategory(
                testDataBuilder.getAdminRoleByTeam(testTeam).getId(),
                testTeam.getId(),
                "테스트 카테고리",
                new HashSet<>()
        );

        // 탈퇴시킬 멤버가 게시글 작성
        Post post = testDataBuilder.createPost(
                "테스트 게시글",
                "내용",
                member2,
                category,
                testTeam,
                testTeamMember
        );

        RemoveMemberRequestDTO request = new RemoveMemberRequestDTO(true);

        // when & then
        mockMvc.perform(delete("/teams/{teamId}/members/{teamMemberId}",
                        testTeam.getId(), testTeamMember.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(userPrincipal)))
                .andExpect(status().isOk());

        // 멤버가 삭제되었는지 확인
        assertThat(teamMemberRepository.findById(testTeamMember.getId())).isEmpty();

        // 게시글도 함께 삭제되었는지 확인
        assertThat(postRepository.findById(post.getId())).isEmpty();
    }

    @Test
    @WithMockTeamPermission(teamPermissions = {"MANAGE_MEMBERS"})
    @DisplayName("팀 멤버 강제 탈퇴 성공 - 게시글/댓글 익명 처리")
    void removeMember_anonymizeContent_success() throws Exception {
        // given
        UserPrincipal userPrincipal = testDataBuilder.getCurrentUserPrincipal();
        TeamCategory category = testDataBuilder.createCategory(
                testDataBuilder.getAdminRoleByTeam(testTeam).getId(),
                testTeam.getId(),
                "테스트 카테고리",
                new HashSet<>()
        );

        // 탈퇴시킬 멤버가 게시글 작성
        Post post = testDataBuilder.createPost(
                "테스트 게시글",
                "내용",
                member2,
                category,
                testTeam,
                testTeamMember
        );

        Long postId = post.getId();
        RemoveMemberRequestDTO request = new RemoveMemberRequestDTO(false);

        // when & then
        mockMvc.perform(delete("/teams/{teamId}/members/{teamMemberId}",
                        testTeam.getId(), testTeamMember.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(userPrincipal)))
                .andExpect(status().isOk());

        // 멤버가 삭제되었는지 확인
        assertThat(teamMemberRepository.findById(testTeamMember.getId())).isEmpty();

        // 게시글은 유지되고 작성자만 null인지 확인
        Post updatedPost = postRepository.findById(postId).orElseThrow();
        assertThat(updatedPost.getTeamMember()).isNull();
    }

    @Test
    @WithMockTeamPermission(teamPermissions = {"MANAGE_MEMBERS"})
    @DisplayName("팀 멤버 강제 탈퇴 실패 - 팀 소유자는 탈퇴 불가")
    void removeMember_ownerCannotBeRemoved_badRequest() throws Exception {
        // given
        UserPrincipal userPrincipal = testDataBuilder.getCurrentUserPrincipal();
        TeamMember ownerTeamMember = testDataBuilder.getTeamMember(
                testTeam.getId(),
                member1.getMemberId()
        );
        RemoveMemberRequestDTO request = new RemoveMemberRequestDTO(true);

        // when & then
        mockMvc.perform(delete("/teams/{teamId}/members/{teamMemberId}",
                        testTeam.getId(), ownerTeamMember.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(userPrincipal)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("팀 소유자는 강제 탈퇴시킬 수 없습니다."));
    }

    @Test
    @WithMockTeamPermission
    @DisplayName("팀 멤버 강제 탈퇴 실패 - 권한 없음")
    void removeMember_noPermission_forbidden() throws Exception {
        // given
        UserPrincipal userPrincipal = testDataBuilder.getCurrentUserPrincipal();
        RemoveMemberRequestDTO request = new RemoveMemberRequestDTO(true);

        // when & then
        mockMvc.perform(delete("/teams/{teamId}/members/{teamMemberId}",
                        testTeam.getId(), testTeamMember.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(userPrincipal)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("팀 멤버 강제 탈퇴 실패 - 인증되지 않은 사용자")
    void removeMember_unauthenticated_unauthorized() throws Exception {
        // given
        RemoveMemberRequestDTO request = new RemoveMemberRequestDTO(true);

        // when & then
        mockMvc.perform(delete("/teams/{teamId}/members/{teamMemberId}",
                        testTeam.getId(), testTeamMember.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockTeamPermission(teamPermissions = {"MANAGE_MEMBERS"})
    @DisplayName("팀 멤버 강제 탈퇴 실패 - 존재하지 않는 팀")
    void removeMember_teamNotFound_notFound() throws Exception {
        // given
        UserPrincipal userPrincipal = testDataBuilder.getCurrentUserPrincipal();
        Long nonExistentTeamId = 999L;
        RemoveMemberRequestDTO request = new RemoveMemberRequestDTO(true);

        // when & then
        mockMvc.perform(delete("/teams/{teamId}/members/{teamMemberId}",
                        nonExistentTeamId, testTeamMember.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(userPrincipal)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("team not found"));
    }

    @Test
    @WithMockTeamPermission(teamPermissions = {"MANAGE_MEMBERS"})
    @DisplayName("팀 멤버 강제 탈퇴 실패 - 존재하지 않는 멤버")
    void removeMember_memberNotFound_notFound() throws Exception {
        // given
        UserPrincipal userPrincipal = testDataBuilder.getCurrentUserPrincipal();
        Long nonExistentMemberId = 999L;
        RemoveMemberRequestDTO request = new RemoveMemberRequestDTO(true);

        // when & then
        mockMvc.perform(delete("/teams/{teamId}/members/{teamMemberId}",
                        testTeam.getId(), nonExistentMemberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(userPrincipal)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("team member not found"));
    }

    @Test
    @WithMockTeamPermission(teamPermissions = {"MANAGE_MEMBERS"})
    @DisplayName("팀 멤버 강제 탈퇴 실패 - 다른 팀의 멤버")
    void removeMember_memberFromDifferentTeam_badRequest() throws Exception {
        // given
        UserPrincipal userPrincipal = testDataBuilder.getCurrentUserPrincipal();
        Member member3 = testDataBuilder.createMember("third@member.com", "third");
        Team anotherTeam = testDataBuilder.createTeam(member2);
        TeamMember anotherTeamMember = testDataBuilder.addMemberToTeam(
                member3,
                anotherTeam.getId()
        );
        RemoveMemberRequestDTO request = new RemoveMemberRequestDTO(true);

        // when & then
        mockMvc.perform(delete("/teams/{teamId}/members/{teamMemberId}",
                        testTeam.getId(), anotherTeamMember.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(userPrincipal)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("해당 멤버는 이 팀에 속하지 않습니다."));
    }

    @Test
    @WithMockTeamPermission(teamPermissions = {"MANAGE_MEMBERS"})
    @DisplayName("팀 멤버 강제 탈퇴 성공 - 댓글도 함께 삭제")
    void removeMember_withComments_deleteAll_success() throws Exception {
        // given
        UserPrincipal userPrincipal = testDataBuilder.getCurrentUserPrincipal();
        TeamCategory category = testDataBuilder.createCategory(
                testDataBuilder.getAdminRoleByTeam(testTeam).getId(),
                testTeam.getId(),
                "테스트 카테고리",
                new HashSet<>()
        );

        // 다른 사람이 게시글 작성
        TeamMember owner = testDataBuilder.getTeamMember(testTeam.getId(), member1.getMemberId());
        Post post = testDataBuilder.createPost(
                "테스트 게시글",
                "내용",
                member1,
                category,
                testTeam,
                owner
        );

        // 탈퇴시킬 멤버가 댓글 작성
        Comment comment = testDataBuilder.createComment(
                "테스트 댓글", post, member2, testTeamMember
        );

        Long commentId = comment.getId();
        RemoveMemberRequestDTO request = new RemoveMemberRequestDTO(true);

        // when & then
        mockMvc.perform(delete("/teams/{teamId}/members/{teamMemberId}",
                        testTeam.getId(), testTeamMember.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(userPrincipal)))
                .andExpect(status().isOk());

        // 멤버가 삭제되었는지 확인
        assertThat(teamMemberRepository.findById(testTeamMember.getId())).isEmpty();

        // 댓글도 함께 삭제되었는지 확인
        assertThat(commentRepository.findById(commentId)).isEmpty();

        // 게시글은 유지되는지 확인
        assertThat(postRepository.findById(post.getId())).isPresent();
    }

    @Test
    @WithMockTeamPermission(teamPermissions = {"MANAGE_MEMBERS"})
    @DisplayName("팀 멤버 강제 탈퇴 성공 - 여러 게시글/댓글 함께 삭제")
    void removeMember_multipleContents_deleteAll_success() throws Exception {
        // given
        UserPrincipal userPrincipal = testDataBuilder.getCurrentUserPrincipal();
        TeamCategory category = testDataBuilder.createCategory(
                testDataBuilder.getAdminRoleByTeam(testTeam).getId(),
                testTeam.getId(),
                "테스트 카테고리",
                new HashSet<>()
        );

        // 탈퇴시킬 멤버가 여러 게시글 작성
        Post post1 = testDataBuilder.createPost(
                "게시글1", "내용1", member2, category, testTeam, testTeamMember
        );
        Post post2 = testDataBuilder.createPost(
                "게시글2", "내용2", member2, category, testTeam, testTeamMember
        );
        Post post3 = testDataBuilder.createPost(
                "게시글3", "내용3", member2, category, testTeam, testTeamMember
        );

        // 댓글도 작성
        Comment comment1 = testDataBuilder.createComment("댓글1", post1, member2, testTeamMember);
        Comment comment2 = testDataBuilder.createComment("댓글2", post2, member2, testTeamMember);

        RemoveMemberRequestDTO request = new RemoveMemberRequestDTO(true);

        // when & then
        mockMvc.perform(delete("/teams/{teamId}/members/{teamMemberId}",
                        testTeam.getId(), testTeamMember.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(userPrincipal)))
                .andExpect(status().isOk());

        // 멤버가 삭제되었는지 확인
        assertThat(teamMemberRepository.findById(testTeamMember.getId())).isEmpty();

        // 모든 게시글과 댓글이 삭제되었는지 확인
        assertThat(postRepository.findById(post1.getId())).isEmpty();
        assertThat(postRepository.findById(post2.getId())).isEmpty();
        assertThat(postRepository.findById(post3.getId())).isEmpty();
        assertThat(commentRepository.findById(comment1.getId())).isEmpty();
        assertThat(commentRepository.findById(comment2.getId())).isEmpty();
    }

    @Test
    @WithMockTeamPermission(teamPermissions = {"MANAGE_MEMBERS"})
    @DisplayName("팀 멤버 강제 탈퇴 실패 - null request body")
    void removeMember_nullRequestBody_badRequest() throws Exception {
        // given
        UserPrincipal userPrincipal = testDataBuilder.getCurrentUserPrincipal();

        // when & then
        mockMvc.perform(delete("/teams/{teamId}/members/{teamMemberId}",
                        testTeam.getId(), testTeamMember.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(userPrincipal)))
                .andExpect(status().isBadRequest());
    }
}
