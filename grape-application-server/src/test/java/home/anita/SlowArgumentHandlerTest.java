package home.anita;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SlowArgumentHandlerTest {

    @Autowired
    private SlowArgumentHandler slowArgumentHandler;

    @AfterEach
    void cleanUp() {
        System.clearProperty("app.slow.enabled");
    }

    @Test
    void testConfigureEnabled() {
        String[] args = {"--slow=true"};
        
        slowArgumentHandler.configure(args);
        
        assertEquals("true", System.getProperty("app.slow.enabled"));
    }

    @Test
    void testConfigureDisabled() {
        String[] args = {"--slow=false"};
        
        slowArgumentHandler.configure(args);
        
        assertNull(System.getProperty("app.slow.enabled"));
    }

    @Test
    void testConfigureNotSpecified() {
        String[] args = {};
        
        slowArgumentHandler.configure(args);
        
        assertNull(System.getProperty("app.slow.enabled"));
    }
    
    @Test
    void testSlowFeatureEnabledDefaultValue() {
        assertFalse(SlowArgumentHandler.SLOW_FEATURE_ENABLED_DEFAULT_VALUE);
    }
}