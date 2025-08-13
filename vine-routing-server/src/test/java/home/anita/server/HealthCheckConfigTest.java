package home.anita.server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HealthCheckConfigTest {

    @Test
    void testDefaultInterval() {
        HealthCheckConfig config = new HealthCheckConfig();
        assertEquals(10000, config.getInterval());
    }

    @Test
    void testSetInterval() {
        HealthCheckConfig config = new HealthCheckConfig();
        config.setInterval(5000);
        assertEquals(5000, config.getInterval());
    }

    @Test
    void testToString() {
        HealthCheckConfig config = new HealthCheckConfig();
        config.setInterval(15000);
        assertEquals("HealthCheckConfig{interval=15000ms}", config.toString());
    }
}