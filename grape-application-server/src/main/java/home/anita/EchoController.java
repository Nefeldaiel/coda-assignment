package home.anita;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EchoController {

    @Value("${server.port:8080}")
    private int serverPort;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/api/echo")
    public ResponseEntity<String> echo(@RequestBody String requestBody) {
        try {
            JsonNode jsonNode = objectMapper.readTree(requestBody);
            if (jsonNode.isObject()) {
                ObjectNode objectNode = (ObjectNode) jsonNode;
                objectNode.put("port", String.valueOf(serverPort));
                return ResponseEntity.ok(objectMapper.writeValueAsString(objectNode));
            } else {
                return ResponseEntity.ok(requestBody);
            }
        } catch (Exception e) {
            return ResponseEntity.ok(requestBody);
        }
    }
}