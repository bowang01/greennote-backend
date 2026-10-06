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

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NoteUpdateTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NoteMapper noteMapper;

    @Test
    void authorCanResubmitADraftOrPendingNote() throws Exception {
        String token = member("editor", "Editor");
        String other = member("othereditor", "Other");
        String channelId = idNamed("/api/channels", "Fashion");
        String topicId = idNamed("/api/topics", "Weekend Trip");
        String otherTopicId = idNamed("/api/topics", "Outfit of the Day");

        MvcResult created = mockMvc.perform(post("/api/member/notes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "First draft",
                                  "content": "Before",
                                  "channelId": "%s",
                                  "topicIds": ["%s"],
                                  "imageUrls": ["/uploads/old.png"]
                                }
                                """.formatted(channelId, topicId)))
                .andExpect(status().isOk())
                .andReturn();
        String noteId = objectMapper.readTree(created.getResponse().getContentAsString()).get("data").asText();

        String body = """
                {
                  "title": "Revised title",
                  "content": "After",
                  "channelId": "%s",
                  "topicIds": ["%s"],
                  "imageUrls": ["/uploads/new.png"],
                  "cityName": "Wellington"
                }
                """.formatted(channelId, otherTopicId);

        if (noteMapper.findById(noteId).status() == 2) {
            mockMvc.perform(put("/api/member/notes/" + noteId)
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.msg").value("Note cannot be edited"));
            noteMapper.reject(noteId, "Need a clearer title");
        }

        mockMvc.perform(put("/api/member/notes/" + noteId)
                        .header("Authorization", "Bearer " + other)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/api/member/notes/" + noteId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/notes/" + noteId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Revised title"))
                .andExpect(jsonPath("$.data.content").value("After"))
                .andExpect(jsonPath("$.data.status").value(1))
                .andExpect(jsonPath("$.data.rejectReason").value(nullValue()))
                .andExpect(jsonPath("$.data.cityName").value("Wellington"))
                .andExpect(jsonPath("$.data.imageUrls[0]").value("/uploads/new.png"))
                .andExpect(jsonPath("$.data.topics[0].name").value("Outfit of the Day"))
                .andExpect(jsonPath("$.data.topics[1]").doesNotExist());
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
