package com.greennote;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void siteIsPublicAndAdminCanChangeIt() throws Exception {
        mockMvc.perform(get("/api/site"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("GreenNote"));

        String token = adminToken();
        mockMvc.perform(put("/api/admin/site")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Note Lab\",\"logo\":\"\",\"themeColor\":\"#2255aa\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Note Lab"))
                .andExpect(jsonPath("$.data.themeColor").value("#2255aa"));
    }

    @Test
    void memberCanRegisterAndUpdateProfile() throws Exception {
        mockMvc.perform(post("/api/member/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ada\",\"password\":\"secret1\",\"nickname\":\"Ada\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        MvcResult login = mockMvc.perform(post("/api/member/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ada\",\"password\":\"secret1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("ada"))
                .andReturn();
        String token = objectMapper.readTree(login.getResponse().getContentAsString()).get("data").get("accessToken").asText();

        MockMultipartFile file = new MockMultipartFile("file", "face.png", "image/png", new byte[]{1, 2, 3});
        MvcResult uploaded = mockMvc.perform(multipart("/api/member/files").file(file).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.url").exists())
                .andReturn();
        String url = objectMapper.readTree(uploaded.getResponse().getContentAsString()).get("data").get("url").asText();

        mockMvc.perform(put("/api/member/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"Ada Lovelace\",\"bio\":\"Notes\",\"avatar\":\"" + url + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("Ada Lovelace"))
                .andExpect(jsonPath("$.data.avatar").value(url));

        String admin = adminToken();
        mockMvc.perform(get("/api/admin/members").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1));

        long memberId = objectMapper.readTree(
                mockMvc.perform(get("/api/member/profile").header("Authorization", "Bearer " + token))
                        .andReturn().getResponse().getContentAsString()
        ).get("data").get("userId").asLong();

        mockMvc.perform(patch("/api/admin/members/" + memberId + "/status")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":1}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/member/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ada\",\"password\":\"secret1\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.msg").value("Account is disabled"));
    }

    @Test
    void adminCanReadCatalog() throws Exception {
        String token = adminToken();
        mockMvc.perform(get("/api/admin/departments").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Headquarters"));
        mockMvc.perform(get("/api/admin/dictionaries").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].value").value("0"));
        mockMvc.perform(get("/api/admin/logs").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].operatorName").value("admin"));
    }

    private String adminToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("data").get("accessToken").asText();
    }
}
