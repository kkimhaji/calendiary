package com.example.board.category;

import com.example.board.domain.member.Member;
import com.example.board.domain.member.MemberRepository;
import com.example.board.domain.role.TeamRole;
import com.example.board.domain.team.Team;
import com.example.board.domain.teamMember.TeamMember;
import com.example.board.dto.category.CategoryRolePermissionDTO;
import com.example.board.dto.category.CreateCategoryRequest;
import com.example.board.service.TeamMemberService;
import com.example.board.support.TestDataBuilder;
import com.example.board.support.WithMockCustomUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.attribute.UserPrincipal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

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
        CategoryRolePermissionDTO request = new CategoryRolePermissionDTO(role.getId(), new HashSet<>(Arrays.asList(VIEW_POST, DELETE_POST, CREATE_POST, CREATE_COMMENT, EDIT_POST, DELETE_COMMENT)));

        // when & then
        mockMvc.perform(post("/teams/{teamId}/categories/create", teamId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

}
