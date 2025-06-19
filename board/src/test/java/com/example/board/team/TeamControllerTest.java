package com.example.board.team;

import com.example.board.auth.UserPrincipal;
import com.example.board.config.security.WithMockTeamPermission;
import com.example.board.member.MemberRepository;
import com.example.board.support.AbstractControllerTestSupport;
import com.example.board.support.TestDataBuilder;
import com.example.board.team.dto.TeamCreateRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class TeamControllerTest extends AbstractControllerTestSupport {
    @Autowired private MockMvc mockMvc;
    @Autowired private TestDataBuilder builder;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void createTeamTest() throws Exception {
        var request = objectMapper.writeValueAsString(new TeamCreateRequestDTO("test team", ""));
        mockMvc.perform(post("/team/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(principal1))
                .content(request))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockTeamPermission
    void updateTeamTest(){

    }


}
