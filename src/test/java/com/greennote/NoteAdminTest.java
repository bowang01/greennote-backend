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

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NoteAdminTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void adminCanReviewAndHideANote() throws Exception {
        String author = member("reviewauthor", "Review Author");
        String admin = adminToken();
        String channelId = idNamed("/api/channels", "Beauty");
        String noteId = publish(author, channelId, "Review me");

        mockMvc.perform(get("/api/admin/notes").param("channelId", channelId)
                        .header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[?(@.id == '" + noteId + "')].title").value("Review me"));

        mockMvc.perform(put("/api/admin/notes/" + noteId + "/reject")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Too short\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/notes/" + noteId))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/notes/" + noteId).header("Authorization", "Bearer " + author))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(1))
                .andExpect(jsonPath("$.data.rejectReason").value("Too short"));

        mockMvc.perform(put("/api/admin/notes/" + noteId + "/approve")
                        .header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/notes/" + noteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(2))
                .andExpect(jsonPath("$.data.rejectReason").value(nullValue()));

        mockMvc.perform(put("/api/admin/notes/" + noteId + "/offline")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Removed\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/notes/" + noteId))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/admin/notes").param("status", "3").param("channelId", channelId)
                        .header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[?(@.id == '" + noteId + "')].status").value(3));

        mockMvc.perform(put("/api/admin/notes/missing-note/approve")
                        .header("Authorization", "Bearer " + admin))
                .andExpect(status().isNotFound());
    }

    private String publish(String token, String channelId, String title) throws Exception {
        MvcResult created = mockMvc.perform(post("/api/member/notes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "%s",
                                  "content": "Details",
                                  "channelId": "%s",
                                  "imageUrls": ["/uploads/review.png"]
                                }
                                """.formatted(title, channelId)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(created.getResponse().getContentAsString()).get("data").asText();
    }

    private String member(String username, String nickname) throws Exception {
        mockMvc.perform(post("/api/member/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"secret1\",\"nickname\":\"" + nickname + "\"}"))
                .andExpect(status().isOk());
        MvcResult login = mockMvc.perform(post("/api/member/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"secret1\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(login.getResponse().getContentAsString()).get("data").get("accessToken").asText();
    }

    private String adminToken() throws Exception {
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(login.getResponse().getContentAsString()).get("data").get("accessToken").asText();
    }

    private String idNamed(String path, String name) throws Exception {
        MvcResult result = mockMvc.perform(get(path)).andExpect(status().isOk()).andReturn();
        for (JsonNode row : objectMapper.readTree(result.getResponse().getContentAsString()).get("data")) {
            if (name.equals(row.get("name").asText())) {
                return row.get("id").asText();
            }
        }
        throw new AssertionError("Missing " + name);
    }
}
