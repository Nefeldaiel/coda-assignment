package home.anita;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoutingController.class)
class RoutingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoutingService routingService;

    @MockBean
    private RoutingConfig routingConfig;

    @Test
    void testPostRequestRouting() throws Exception {
        String requestBody = "{\"message\": \"test\"}";
        String responseBody = "{\"message\": \"test\", \"port\": \"9001\"}";
        
        when(routingService.routeRequest(eq(requestBody), any(HttpHeaders.class), eq("/api/echo"), any()))
            .thenReturn(ResponseEntity.ok(responseBody));

        mockMvc.perform(post("/api/echo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().string(responseBody));
    }

    @Test
    void testGetRequestReturns405() throws Exception {
        mockMvc.perform(get("/api/echo"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void testPutRequestReturns405() throws Exception {
        mockMvc.perform(put("/api/echo")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void testDeleteRequestReturns405() throws Exception {
        mockMvc.perform(delete("/api/echo"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void testErrorPropagation404() throws Exception {
        String requestBody = "{\"message\": \"test\"}";
        
        when(routingService.routeRequest(eq(requestBody), any(HttpHeaders.class), eq("/nonexistent"), any()))
            .thenReturn(ResponseEntity.notFound().build());

        mockMvc.perform(post("/nonexistent")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    void testErrorPropagation500() throws Exception {
        String requestBody = "{\"message\": \"test\"}";
        String errorBody = "Internal server error";
        
        when(routingService.routeRequest(eq(requestBody), any(HttpHeaders.class), eq("/api/echo"), any()))
            .thenReturn(ResponseEntity.internalServerError().body(errorBody));

        mockMvc.perform(post("/api/echo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(errorBody));
    }
}