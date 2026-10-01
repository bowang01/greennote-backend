package com.greennote;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ChannelTopicFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void publicListsOnlyEnabledRowsAndAdminCanChangeThem() throws Exception {
        mockMvc.perform(get("/api/channels"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].name", hasItem("Fashion")))
                .andExpect(jsonPath("$.data[*].status", not(hasItem(1))));

        mockMvc.perform(get("/api/topics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].name", hasItem("Weekend Trip")));

        String token = adminToken();

        mockMvc.perform(post("/api/admin/channels")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Night Market\",\"sortNo\":9}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/admin/channels")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Night Market\",\"sortNo\":9}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("Channel name already exists"));

        String channelId = idOf(token, "/api/admin/channels", "Night Market");

        mockMvc.perform(put("/api/admin/channels/" + channelId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Night Market\",\"sortNo\":12}"))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/admin/channels/" + channelId + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":1}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/channels"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].name", not(hasItem("Night Market"))));

        mockMvc.perform(get("/api/admin/channels").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.name == 'Night Market')].status").value(1));

        mockMvc.perform(put("/api/admin/channels/999999")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Missing\",\"sortNo\":1}"))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/admin/topics")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"City Walk\",\"intro\":\"\",\"sortNo\":8}"))
                .andExpect(status().isOk());

        String topicId = idOf(token, "/api/admin/topics", "City Walk");

        mockMvc.perform(patch("/api/admin/topics/" + topicId + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":1}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/topics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].name", not(hasItem("City Walk"))));
    }

    private String idOf(String token, String path, String name) throws Exception {
        MvcResult result = mockMvc.perform(get(path).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        for (JsonNode row : objectMapper.readTree(result.getResponse().getContentAsString()).get("data")) {
            if (name.equals(row.get("name").asText())) {
                return row.get("id").asText();
            }
        }
        throw new AssertionError("Missing " + name);
    }

    private String adminToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("data").get("accessToken").asText();
    }
}
