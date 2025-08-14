package com.example.board.member;

import com.example.board.auth.UserPrincipal;
import com.example.board.comment.CommentService;
import com.example.board.comment.dto.MemberCommentResponse;
import com.example.board.member.dto.MemberInfoResponse;
import com.example.board.member.dto.MemberInfoSummaryResponse;
import com.example.board.member.dto.PasswordChangeRequest;
import com.example.board.member.dto.VerifyPasswordRequest;
import com.example.board.post.PostService;
import com.example.board.post.dto.PostListResponse;
import com.example.board.support.AbstractControllerTestSupport;
import com.example.board.team.dto.TeamListDTO;
import com.example.board.teamMember.TeamMemberService;
import com.example.board.teamMember.dto.MemberProfileResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class MemberControllerTest extends AbstractControllerTestSupport {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private TeamMemberService teamMemberService;
    @MockBean
    private PostService postService;
    @MockBean
    private CommentService commentService;
    private UserPrincipal testPrincipal;

    @BeforeEach
    void init(){
        testPrincipal = new UserPrincipal(member1);
        Authentication auth = new UsernamePasswordAuthenticationToken(testPrincipal, null, testPrincipal.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    @Test
    @DisplayName("getPrincipal - 로그인 사용자 정보 반환")
    void getPrincipal_success() throws Exception {
        mockMvc.perform(get("/member/getprincipal")
                        .principal(() -> testPrincipal.getUsername()) // SecurityContext 모킹
                        .requestAttr("userPrincipal", testPrincipal))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("getTeams - 사용자 팀 목록 반환")
    void getTeams_success() throws Exception {
        List<TeamListDTO> teams = List.of(new TeamListDTO(1L, "teamA"));
        given(teamMemberService.getTeams(any(Member.class))).willReturn(teams);

        mockMvc.perform(get("/member/get_teams")
                        .requestAttr("userPrincipal", testPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("teamA"));
    }

    @Test
    @DisplayName("getMemberInfo - 간략 회원 정보 반환")
    void getMemberInfo_success() throws Exception {
        MemberInfoSummaryResponse resp = new MemberInfoSummaryResponse(1L, "tester");
        given(memberService.getMemberInfoSummary(any(UserPrincipal.class))).willReturn(resp);

        mockMvc.perform(get("/member/get-info")
                        .requestAttr("userPrincipal", testPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nickname").value("tester"));
    }

    @Test
    @DisplayName("changePassword - 비밀번호 변경")
    void changePassword_success() throws Exception {
        PasswordChangeRequest req = new PasswordChangeRequest("newPwd");

        mockMvc.perform(post("/member/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .requestAttr("userPrincipal", testPrincipal)
                        .content(new ObjectMapper().writeValueAsString(req)))
                .andExpect(status().isOk());

        then(memberService).should().updatePassword(member1, "newPwd");
    }


    @Test
    @DisplayName("getAccountInfo - 계정 정보 반환")
    void getAccountInfo_success() throws Exception {
        MemberInfoResponse resp = new MemberInfoResponse(1L, "test@example.com", "tester");
        given(memberService.getInfoForAccountPage(any(UserPrincipal.class))).willReturn(resp);

        mockMvc.perform(get("/member/account-info")
                        .requestAttr("userPrincipal", testPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.nickname").value("tester"));
    }

    @Test
    @DisplayName("verifyPassword - 비밀번호 확인")
    void verifyPassword_success() throws Exception {
        VerifyPasswordRequest req = new VerifyPasswordRequest("currentPwd");
        given(memberService.checkPassword(any(Member.class), eq("currentPwd"))).willReturn(true);

        mockMvc.perform(post("/member/verify-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .requestAttr("userPrincipal", testPrincipal)
                        .content(new ObjectMapper().writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("leaveTeam - 팀 나가기")
    void leaveTeam_success() throws Exception {
        mockMvc.perform(post("/member/1/leave")
                        .param("deleteContents", "true")
                        .requestAttr("userPrincipal", testPrincipal))
                .andExpect(status().isOk());

        then(teamMemberService).should().leaveTeam(1L, member1, true);
    }


    @Test
    @DisplayName("getTeamMemberProfile - 팀 멤버 프로필 조회")
    void getTeamMemberProfile_success() throws Exception {
        LocalDateTime now = LocalDateTime.of(2025, 8, 14, 10, 0, 0);
        MemberProfileResponse profile = new MemberProfileResponse(
                "test@example.com", // email
                "teamNick",         // teamNickname
                "roleName",         // roleName
                now                 // joinedAt
        );
        given(teamMemberService.getTeamMemberProfile(2L)).willReturn(profile);

        // when & then
        mockMvc.perform(get("/member/teams/1/member/2")
                        .with(user(testPrincipal))) // 인증 필요 시 추가
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.teamNickname").value("teamNick"))
                .andExpect(jsonPath("$.roleName").value("roleName"))
                .andExpect(jsonPath("$.joinedAt").value(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))));
    }
}
