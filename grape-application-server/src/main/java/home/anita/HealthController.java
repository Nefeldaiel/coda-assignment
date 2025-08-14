package home.anita;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller that provides health check endpoints.
 * Used to monitor application status and availability.
 */
@RestController
public class HealthController {

    /**
     * Simple health check endpoint that returns application status.
     * Returns HTTP 200 with status "UP" to indicate healthy state.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        return ResponseEntity.ok(status);
    }
}