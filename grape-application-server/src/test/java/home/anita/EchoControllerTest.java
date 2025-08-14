package home.anita;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EchoController.class)
class EchoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppConfig appConfig;


    @BeforeEach
    void setUp() {
        AppConfig.Slow slowConfig = new AppConfig.Slow();
        slowConfig.setEnabled(false);
        slowConfig.setSleepTimeMs(800L);

        when(appConfig.getSlow()).thenReturn(slowConfig);
    }

    @Test
    void testEchoWithPlainText() throws Exception {
        String requestBody = "Hello, World!";

        mockMvc.perform(post("/api/echo")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().string(requestBody));
    }

    @Test
    void testEchoWithEmptyBody() throws Exception {
        String requestBody = "   ";

        mockMvc.perform(post("/api/echo")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().string(requestBody));
    }

    @Test
    void testEchoWithJsonContent() throws Exception {
        String requestBody = "{\"message\": \"test\", \"value\": 123}";

        mockMvc.perform(post("/api/echo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("test"))
                .andExpect(jsonPath("$.value").value(123))
                .andExpect(jsonPath("$.port").exists())
                .andExpect(jsonPath("$.port").isString());
    }

    @Test
    void testEchoWithComplexJsonObject() throws Exception {
        String requestBody = "{\"user\": {\"name\": \"John\", \"age\": 30}, \"active\": true}";

        mockMvc.perform(post("/api/echo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.name").value("John"))
                .andExpect(jsonPath("$.user.age").value(30))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.port").exists())
                .andExpect(jsonPath("$.port").isString());
    }

    @Test
    void testEchoWithInvalidJson() throws Exception {
        String requestBody = "invalid json {";

        mockMvc.perform(post("/api/echo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().string(requestBody));
    }

    @Test
    void testWrongPathReturns404() throws Exception {
        String requestBody = "Hello, World!";

        mockMvc.perform(post("/api/wrong")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    void testRootPathReturns404() throws Exception {
        String requestBody = "Hello, World!";

        mockMvc.perform(post("/")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    void testNestedPathReturns404() throws Exception {
        String requestBody = "Hello, World!";

        mockMvc.perform(post("/api/echo/extra")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetMethodReturns405() throws Exception {
        mockMvc.perform(get("/api/echo"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void testPutMethodReturns405() throws Exception {
        String requestBody = "Hello, World!";

        mockMvc.perform(put("/api/echo")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(requestBody))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void testDeleteMethodReturns405() throws Exception {
        mockMvc.perform(delete("/api/echo"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void testSlowFeatureEnabled() throws Exception {
        AppConfig.Slow slowConfig = new AppConfig.Slow();
        slowConfig.setEnabled(true);
        slowConfig.setSleepTimeMs(100L);

        when(appConfig.getSlow()).thenReturn(slowConfig);

        String requestBody = "test";
        long startTime = System.currentTimeMillis();

        mockMvc.perform(post("/api/echo")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().string(requestBody));

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Should take at least 100ms due to sleep
        assert (duration >= 100);
    }

    @Test
    void testPortReflectsActualServerPort() throws Exception {
        String requestBody = "{\"message\": \"test\"}";

        mockMvc.perform(post("/api/echo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("test"))
                .andExpect(jsonPath("$.port").value("8765"));  // Should reflect actual server port
    }
}