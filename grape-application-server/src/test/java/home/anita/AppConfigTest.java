package home.anita;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AppConfigTest {

    @Test
    void testDefaultPortConfiguration() {
        AppConfig config = new AppConfig();
        
        assertNotNull(config.getPort());
        assertEquals(AppConfig.DEFAULT_START_PORT, config.getPort().getStart());
        assertEquals(AppConfig.DEFAULT_MAX_PORT, config.getPort().getMax());
    }

    @Test
    void testPortSettersAndGetters() {
        AppConfig config = new AppConfig();
        AppConfig.Port port = new AppConfig.Port();
        
        port.setStart(8080);
        port.setMax(8090);
        config.setPort(port);
        
        assertEquals(8080, config.getPort().getStart());
        assertEquals(8090, config.getPort().getMax());
    }

    @Test
    void testPortClassDefaultValues() {
        AppConfig.Port port = new AppConfig.Port();
        
        assertEquals(AppConfig.DEFAULT_START_PORT, port.getStart());
        assertEquals(AppConfig.DEFAULT_MAX_PORT, port.getMax());
    }

    @Test
    void testPortClassSettersAndGetters() {
        AppConfig.Port port = new AppConfig.Port();
        
        port.setStart(3000);
        port.setMax(4000);
        
        assertEquals(3000, port.getStart());
        assertEquals(4000, port.getMax());
    }
}