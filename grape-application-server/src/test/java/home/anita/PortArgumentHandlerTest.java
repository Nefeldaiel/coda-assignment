package home.anita;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PortArgumentHandlerTest {

    @Autowired
    private PortArgumentHandler portArgumentHandler;

    @AfterEach
    void cleanUp() {
        System.clearProperty("server.port.explicit");
    }

    @Test
    void testConfigurePortWithExplicit() {
        String[] args = {"--port", "8080"};
        
        portArgumentHandler.configure(args);
        
        assertEquals("8080", System.getProperty("server.port.explicit"));
    }

    @Test
    void testConfigurePortWithoutExplicit() {
        String[] args = {};
        
        portArgumentHandler.configure(args);
        
        assertNull(System.getProperty("server.port.explicit"));
    }

    @Test
    void testConfigurePortWithInvalid() {
        String[] args = {"--port", "invalid"};
        
        portArgumentHandler.configure(args);
        
        assertNull(System.getProperty("server.port.explicit"));
    }

    @Test
    void testNoPortSpecifiedConstant() {
        assertEquals(-1, PortArgumentHandler.NO_PORT_SPECIFIED);
    }
}