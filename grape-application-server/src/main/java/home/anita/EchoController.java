package home.anita;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that provides an echo endpoint for testing purposes.
 * Returns the request body with the actual server port added to JSON objects.
 * Supports optional slow response feature for testing latency scenarios.
 */
@RestController
@RequiredArgsConstructor
public class EchoController {

    private final AppConfig appConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Echo endpoint that returns the request body with the actual server port.
     * For JSON objects, adds a "port" field with the current server port.
     * For non-JSON content, returns the body unchanged.
     * Optionally applies artificial delay if slow feature is enabled.
     */
    @PostMapping("/api/echo")
    public ResponseEntity<String> echo(@RequestBody String requestBody,
                                       HttpServletRequest request) {
        try {
            if (shouldSlowDown()) {
                Thread.sleep(appConfig.getSlow().getSleepTimeMs());
            }

            JsonNode jsonNode = objectMapper.readTree(requestBody);
            if (jsonNode.isObject()) {
                var objectNode = (ObjectNode) jsonNode;
                var actualPort = request.getLocalPort();
                objectNode.put("port", String.valueOf(actualPort));
                return ResponseEntity.ok(objectMapper.writeValueAsString(objectNode));
            } else {
                return ResponseEntity.ok(requestBody);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.ok(requestBody);
        } catch (Exception e) {
            return ResponseEntity.ok(requestBody);
        }
    }

    /**
     * Determines if artificial delay should be applied to the response.
     */
    private boolean shouldSlowDown() {
        return appConfig.getSlow().isEnabled() && appConfig.getSlow().getSleepTimeMs() > 0;
    }
}