package com.greennote;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greennote.system.note.mapper.NoteMapper;
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
class NoteFeedTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NoteMapper noteMapper;

    @Test
    void publishedNotesAppearOnDiscoverAndDetail() throws Exception {
        String token = member("reader", "Reader");
        String channelId = idNamed("/api/channels", "Fashion");
        String topicId = idNamed("/api/topics", "Weekend Trip");

        MvcResult created = mockMvc.perform(post("/api/member/notes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Harbour light",
                                  "content": "Evening walk",
                                  "channelId": "%s",
                                  "topicIds": ["%s"],
                                  "imageUrls": ["/uploads/harbour.png"]
                                }
                                """.formatted(channelId, topicId)))
                .andExpect(status().isOk())
                .andReturn();
        String noteId = objectMapper.readTree(created.getResponse().getContentAsString()).get("data").asText();

        mockMvc.perform(get("/api/notes/" + noteId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(1))
                .andExpect(jsonPath("$.data.title").value("Harbour light"))
                .andExpect(jsonPath("$.data.imageUrls[0]").value("/uploads/harbour.png"))
                .andExpect(jsonPath("$.data.topics[0].name").value("Weekend Trip"))
                .andExpect(jsonPath("$.data.liked").value(false));

        mockMvc.perform(get("/api/notes/" + noteId))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/notes").param("channelId", channelId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[?(@.id == '" + noteId + "')]").isEmpty());

        noteMapper.approve(noteId);

        mockMvc.perform(get("/api/notes").param("channelId", channelId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[?(@.id == '" + noteId + "')].title").value("Harbour light"));

        mockMvc.perform(get("/api/notes/" + noteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(2))
                .andExpect(jsonPath("$.data.authorName").value("Reader"));

        mockMvc.perform(put("/api/member/notes/" + noteId + "/like")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/notes/" + noteId).header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.data.liked").value(true))
                .andExpect(jsonPath("$.data.likeCount").value(1));

        mockMvc.perform(post("/api/member/notes/" + noteId + "/comments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Looks good\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isString());

        mockMvc.perform(get("/api/notes/" + noteId + "/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].content").value("Looks good"))
                .andExpect(jsonPath("$.data[0].authorName").value("Reader"));
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
