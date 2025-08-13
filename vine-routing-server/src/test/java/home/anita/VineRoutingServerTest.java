package home.anita;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "routing.servers[0].url=http://localhost:9001",
    "routing.servers[1].url=http://localhost:9002"
})
class VineRoutingServerTest {

    @Test
    void contextLoads() {
        // Test that Spring context loads successfully
    }
}