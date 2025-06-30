package com.example.board.role;

import com.example.board.config.security.WithMockTeamPermission;
import com.example.board.role.dto.CreateRoleRequest;
import com.example.board.support.AbstractControllerTestSupport;
import com.example.board.support.TestDataBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TeamRoleControllerTest extends AbstractControllerTestSupport {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TestDataBuilder builder;
    @Autowired
    private ObjectMapper mapper;

    @Test
    @WithMockTeamPermission(teamPermissions = {"MANAGE_ROLES"})
    void createRoleTest_expect200OK() throws Exception {
        var request = mapper.writeValueAsString(new CreateRoleRequest("test role", new HashSet<>(), "role for test"));
        Long teamId = builder.getCurrentTestTeamId();
        mockMvc.perform(post("/teams/{teamId}/roles/manage/create", teamId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
        ).andExpect(status().isOk());
    }

    @Test
    @WithMockTeamPermission()
    void createRoleTest_withoutPermission() throws Exception {
        var request = mapper.writeValueAsString(new CreateRoleRequest("test role", new HashSet<>(), "role for test"));
        Long teamId = builder.getCurrentTestTeamId();
        mockMvc.perform(post("/teams/{teamId}/roles/manage/create", teamId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
        ).andExpect(status().isForbidden());
    }

    @Test
    @WithMockTeamPermission(teamPermissions = {"MANAGE_ROLES"})
    void createRoleTest_roleNameNull() throws Exception {
        Long teamId = builder.getCurrentTestTeamId();
        String request = """
        {
            "roleName": null,
            "permissions": [],
            "description": "role for test"
        }
        """;
        mockMvc.perform(post("/teams/{teamId}/roles/manage/create", teamId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
        ).andExpect(status().isBadRequest());
    }
}
