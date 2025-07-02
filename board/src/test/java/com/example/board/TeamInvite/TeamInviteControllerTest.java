package com.example.board.TeamInvite;

import com.example.board.config.security.WithMockCategoryPermission;
import com.example.board.config.security.WithMockTeamPermission;
import com.example.board.support.AbstractControllerTestSupport;
import com.example.board.support.TestDataBuilder;
import com.example.board.teamInvite.dto.InviteCreateRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TeamInviteControllerTest extends AbstractControllerTestSupport {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TestDataBuilder builder;
    @Autowired
    private ObjectMapper objectMapper;
    private Long teamId;

    @Test
    @WithMockTeamPermission(teamPermissions = {"MANAGE_MEMBERS"})
    void teamInviteCreateTest_withPermission() throws Exception {
        teamId = builder.getCurrentTestTeamId();
        var request = objectMapper.writeValueAsString(new InviteCreateRequest(LocalDateTime.now().plusMinutes(5), 1));
        mockMvc.perform(post("/teams/{teamId}/invite", teamId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockTeamPermission
    void teamInviteCreateTest_withoutPermission() throws Exception {
        teamId = builder.getCurrentTestTeamId();
        var request = objectMapper.writeValueAsString(new InviteCreateRequest(LocalDateTime.now().plusMinutes(5), 1));
        mockMvc.perform(post("/teams/{teamId}/invite", teamId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isForbidden());
    }
}
