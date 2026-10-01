package com.greennote;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greennote.system.note.NoteRecord;
import com.greennote.system.note.mapper.NoteMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotePublishTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NoteMapper noteMapper;

    @Test
    void memberCanPublishAPendingImageNote() throws Exception {
        mockMvc.perform(post("/api/member/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"publisher\",\"password\":\"secret1\",\"nickname\":\"Publisher\"}"))
                .andExpect(status().isOk());

        MvcResult login = mockMvc.perform(post("/api/member/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"publisher\",\"password\":\"secret1\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String token = objectMapper.readTree(login.getResponse().getContentAsString()).get("data").get("accessToken").asText();

        String channelId = idNamed("/api/channels", "Fashion", null);
        String topicId = idNamed("/api/topics", "Weekend Trip", null);

        MvcResult created = mockMvc.perform(post("/api/member/notes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Market walk",
                                  "content": "Saturday",
                                  "channelId": "%s",
                                  "topicIds": ["%s"],
                                  "imageUrls": ["/uploads/cover.png", "/uploads/second.png"],
                                  "cityName": "Auckland",
                                  "placeName": "Viaduct"
                                }
                                """.formatted(channelId, topicId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isString())
                .andReturn();
        String noteId = objectMapper.readTree(created.getResponse().getContentAsString()).get("data").asText();

        NoteRecord note = noteMapper.findById(noteId);
        assertEquals("Market walk", note.title());
        assertEquals(1, note.type());
        assertEquals(1, note.status());
        assertEquals("/uploads/cover.png", note.coverUrl());
        assertTrue(note.mediaJson().contains("/uploads/second.png"));
        assertEquals("Auckland", note.cityName());
        assertEquals(1, noteMapper.listTopics(noteId).size());
        assertEquals("Weekend Trip", noteMapper.listTopics(noteId).get(0).name());
    }

    private String idNamed(String path, String name, String token) throws Exception {
        var request = get(path);
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        MvcResult result = mockMvc.perform(request).andExpect(status().isOk()).andReturn();
        for (JsonNode row : objectMapper.readTree(result.getResponse().getContentAsString()).get("data")) {
            if (name.equals(row.get("name").asText())) {
                return row.get("id").asText();
            }
        }
        throw new AssertionError("Missing " + name);
    }
}
