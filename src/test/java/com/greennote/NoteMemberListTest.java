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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NoteMemberListTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void memberListsOwnNotesLikesAndCollects() throws Exception {
        String author = member("listauthor", "List Author");
        String reader = member("listreader", "List Reader");
        String channelId = idNamed("/api/channels", "Food");
        String noteId = publish(author, channelId, "Lunch note");

        mockMvc.perform(get("/api/member/notes/mine").header("Authorization", "Bearer " + author))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].id").value(noteId))
                .andExpect(jsonPath("$.data.list[0].title").value("Lunch note"));

        mockMvc.perform(get("/api/member/notes/mine").header("Authorization", "Bearer " + reader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.list").isEmpty());

        mockMvc.perform(put("/api/member/notes/" + noteId + "/like").header("Authorization", "Bearer " + reader))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/member/notes/" + noteId + "/collect").header("Authorization", "Bearer " + reader))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/member/notes/likes").param("page", "1").param("size", "10")
                        .header("Authorization", "Bearer " + reader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].id").value(noteId));

        mockMvc.perform(get("/api/member/notes/collects").header("Authorization", "Bearer " + reader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].title").value("Lunch note"));

        mockMvc.perform(get("/api/member/notes/likes").param("page", "2").param("size", "10")
                        .header("Authorization", "Bearer " + reader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list").isEmpty());
    }

    private String publish(String token, String channelId, String title) throws Exception {
        MvcResult created = mockMvc.perform(post("/api/member/notes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "%s",
                                  "content": "Bowl",
                                  "channelId": "%s",
                                  "imageUrls": ["/uploads/lunch.png"]
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
