package home.anita;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AppConfigTest {

    @Test
    void testDefaultPortConfiguration() {
        AppConfig config = new AppConfig();
        
        assertNotNull(config.getPort());
        assertEquals(AppConfig.START_PORT_DEFAULT_VALUE, config.getPort().getStart());
        assertEquals(AppConfig.MAX_PORT_DEFAULT_VALUE, config.getPort().getMax());
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
        
        assertEquals(AppConfig.START_PORT_DEFAULT_VALUE, port.getStart());
        assertEquals(AppConfig.MAX_PORT_DEFAULT_VALUE, port.getMax());
    }

    @Test
    void testPortClassSettersAndGetters() {
        AppConfig.Port port = new AppConfig.Port();
        
        port.setStart(3000);
        port.setMax(4000);
        
        assertEquals(3000, port.getStart());
        assertEquals(4000, port.getMax());
    }
    
    @Test
    void testLombokGeneratedMethods() {
        // Test that Lombok generates proper equals, hashCode, toString methods
        AppConfig config1 = new AppConfig();
        AppConfig config2 = new AppConfig();
        
        // Test equals and hashCode
        assertEquals(config1, config2);
        assertEquals(config1.hashCode(), config2.hashCode());
        
        // Test toString contains expected content
        String toString = config1.toString();
        assertTrue(toString.contains("AppConfig"));
        assertTrue(toString.contains("port="));
        assertTrue(toString.contains("slow="));
        
        // Test Port equals/hashCode/toString
        AppConfig.Port port1 = new AppConfig.Port();
        AppConfig.Port port2 = new AppConfig.Port();
        assertEquals(port1, port2);
        assertTrue(port1.toString().contains("Port"));
        
        // Test Slow equals/hashCode/toString  
        AppConfig.Slow slow1 = new AppConfig.Slow();
        AppConfig.Slow slow2 = new AppConfig.Slow();
        assertEquals(slow1, slow2);
        assertTrue(slow1.toString().contains("Slow"));
    }
}