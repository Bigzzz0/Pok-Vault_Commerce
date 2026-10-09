package com.pokevault.modules.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasKey;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Smoke test ของ endpoint ระดับแพลตฟอร์มที่ Docker healthcheck, CI/CD และผู้ตรวจงานใช้:
 * Swagger / OpenAPI, Actuator health และไฟล์ static ต้องเปิดได้โดยไม่ต้องล็อกอิน
 * บูตแอปเต็มตัว (H2 ในเครื่อง, Postgres ใน CI) แต่ไม่อ้างอิงข้อมูลใน data.sql
 * ผู้รับผิดชอบ: สมาชิกคนที่ 5 (Swagger / Docker / CI/CD)
 */
@SpringBootTest
@AutoConfigureMockMvc
class PlatformEndpointsTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("/v3/api-docs: ใช้ข้อมูลจาก OpenApiConfig และมี endpoint ของทุกโมดูล")
    void openApiDocs_DescribeAllModules() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("PokéVault Commerce API"))
                .andExpect(jsonPath("$.info.version").value("v1.0.0"))
                .andExpect(jsonPath("$.paths", hasKey("/api/v1/cards")))
                .andExpect(jsonPath("$.paths", hasKey("/api/v1/accounts")))
                .andExpect(jsonPath("$.paths", hasKey("/api/v1/orders")));
    }

    @Test
    @DisplayName("/swagger-ui.html: guest เปิดได้ (redirect ไปหน้า Swagger UI)")
    void swaggerUi_IsPublic() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/swagger-ui/index.html"));
        mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("/actuator/health: guest เรียกได้และสถานะ UP (ใช้กับ Docker healthcheck)")
    void actuatorHealth_IsPublicAndUp() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("ไฟล์ static (CSS / JS / รูปการ์ด) โหลดได้โดยไม่ต้องล็อกอิน")
    void staticAssets_ArePublic() throws Exception {
        for (String asset : new String[]{"/css/style.css", "/css/ux-improvements.css", "/js/app.js",
                "/js/ux-improvements.js", "/images/cards/card-back.jpg"}) {
            mockMvc.perform(get(asset)).andExpect(status().isOk());
        }
    }
}
