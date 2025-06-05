package com.example.board.category;

import com.example.board.member.Member;
import com.example.board.member.MemberRepository;
import com.example.board.role.TeamRole;
import com.example.board.team.Team;
import com.example.board.teamMember.TeamMember;
import com.example.board.category.dto.CategoryRolePermissionDTO;
import com.example.board.category.dto.CreateCategoryRequest;
import com.example.board.teamMember.TeamMemberService;
import com.example.board.support.TestDataBuilder;
import com.example.board.support.WithMockCustomUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.attribute.UserPrincipal;
import java.util.Arrays;
import java.util.HashSet;

import static com.example.board.permission.CategoryPermission.*;
import static com.example.board.permission.CategoryPermission.DELETE_COMMENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
@ActiveProfiles("test")
public class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TestDataBuilder testDataBuilder;
    private Team team;
    private TeamMember teamMember;
    private TeamRole teamRole;
    private CreateCategoryRequest request;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TeamMemberService teamMemberService;


    protected Member getCurrentMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            Member principal = (Member) authentication.getPrincipal();
            return memberRepository.findById(principal.getMemberId()).orElse(null);
        }
        return null;
    }

    @Test
    @WithMockCustomUser
    void createCategory_권한이_있는_경우() throws Exception {
        // given
        Member testMember = getCurrentMember();
        team = testDataBuilder.createTeam(testMember);
        Long teamId = team.getId();
        TeamRole role = teamMemberService.getCurrentUserRole(teamId, testMember);
        CategoryRolePermissionDTO request = new CategoryRolePermissionDTO(role.getId(), new HashSet<>(Arrays.asList(VIEW_POST, DELETE_POST, CREATE_POST, CREATE_COMMENT, DELETE_COMMENT)));

        // when & then
        mockMvc.perform(post("/teams/{teamId}/categories/create", teamId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

}
