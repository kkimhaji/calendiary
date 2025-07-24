package com.example.board.teamMember;

import com.example.board.auth.UserPrincipal;
import com.example.board.config.security.WithMockTeamPermission;
import com.example.board.member.Member;
import com.example.board.support.AbstractControllerTestSupport;
import com.example.board.support.TestDataBuilder;
import com.example.board.team.Team;
import com.example.board.teamMember.dto.ChangeTeamNicknameRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
            Member additionalMember = testDataBuilder.createMember("test" + i + "@example.com", "testUser" + i, "password");
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
            Member additionalMember = testDataBuilder.createMember("test" + i + "@example.com", "testUser" + i, "password");
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
}
