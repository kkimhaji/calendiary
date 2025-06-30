package com.example.board.team;

import com.example.board.auth.UserPrincipal;
import com.example.board.config.security.WithMockTeamPermission;
import com.example.board.support.AbstractControllerTestSupport;
import com.example.board.support.TestDataBuilder;
import com.example.board.team.dto.TeamCreateRequestDTO;
import com.example.board.team.dto.TeamUpdateRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TeamControllerTest extends AbstractControllerTestSupport {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TestDataBuilder builder;
    @Autowired
    private ObjectMapper objectMapper;

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
    void createTeamTestWithoutUserPrincipal() throws Exception {
        var request = objectMapper.writeValueAsString(new TeamCreateRequestDTO("test team", ""));
        mockMvc.perform(post("/team/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getTeamInfoTest_teamMember() throws Exception {
        Team team = builder.createTeam(member1);
        principal1.setTestTeamId(team.getId());
        mockMvc.perform(get("/team/{teamId}", principal1.getTestTeamId())
                        .with(user(principal1)))
                .andExpect(status().isOk());
    }

    @Test
    void getTeamInfoTest_notTeamMember() throws Exception {
        Team team = builder.createTeam(member1);
        principal1.setTestTeamId(team.getId());
        mockMvc.perform(get("/team/{teamId}", principal1.getTestTeamId())
                        .with(user(principal2)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockTeamPermission
    void updateTeamTest_withPermission() throws Exception {
        var request = objectMapper.writeValueAsString(new TeamUpdateRequestDTO("test team", "update test"));
        mockMvc.perform(put("/team/{teamId}", builder.getCurrentTestTeamId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockTeamPermission(teamPermissions = {})
    void updateTeamTest_withoutPermission() throws Exception {
        var request = objectMapper.writeValueAsString(new TeamUpdateRequestDTO("test team", "update test"));
        mockMvc.perform(put("/team/{teamId}", builder.getCurrentTestTeamId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockTeamPermission
    void deleteTeamTest_withPermission() throws Exception {
        mockMvc.perform(delete("/team/delete/{teamId}", builder.getCurrentTestTeamId()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockTeamPermission(teamPermissions = {})
    void deleteTeamTest_withoutPermission() throws Exception {
        mockMvc.perform(delete("/team/delete/{teamId}", builder.getCurrentTestTeamId()))
                .andExpect(status().isForbidden());
    }

}
