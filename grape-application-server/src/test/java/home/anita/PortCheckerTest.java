package home.anita;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PortCheckerTest {

    @AfterEach
    void cleanUp() {
        System.clearProperty("server.port.explicit");
    }

    @Test
    void testCustomizeWithExplicitPort() {
        System.setProperty("server.port.explicit", "8080");
        ConfigurableWebServerFactory factory = mock(ConfigurableWebServerFactory.class);
        
        // Create PortChecker with mock AppConfig
        AppConfig appConfig = mock(AppConfig.class);
        PortChecker portChecker = new PortChecker(appConfig);
        
        portChecker.customize(factory);
        
        verify(factory).setPort(8080);
        // AppConfig should never be called when explicit port is set
        verify(appConfig, never()).getPort();
    }

    @Test
    void testCustomizeWithDefaultPort() {
        System.clearProperty("server.port.explicit");
        ConfigurableWebServerFactory factory = mock(ConfigurableWebServerFactory.class);
        
        // Mock the port configuration
        AppConfig appConfig = mock(AppConfig.class);
        AppConfig.Port portConfig = mock(AppConfig.Port.class);
        when(appConfig.getPort()).thenReturn(portConfig);
        when(portConfig.getStart()).thenReturn(9001);
        when(portConfig.getMax()).thenReturn(9005);
        
        PortChecker portChecker = new PortChecker(appConfig);
        portChecker.customize(factory);
        
        // Should set a port within the available range
        verify(factory, times(1)).setPort(anyInt());
        // getPort() is called twice - once for getStart() and once for getMax()
        verify(appConfig, times(2)).getPort();
    }

    @Test
    void testCustomizeThrowsExceptionWhenAllPortsInUse() {
        System.clearProperty("server.port.explicit");
        ConfigurableWebServerFactory factory = mock(ConfigurableWebServerFactory.class);
        
        // Mock a very narrow range where ports are likely unavailable
        AppConfig appConfig = mock(AppConfig.class);
        AppConfig.Port portConfig = mock(AppConfig.Port.class);
        when(appConfig.getPort()).thenReturn(portConfig);
        when(portConfig.getStart()).thenReturn(1);  // Port 1 is typically not available
        when(portConfig.getMax()).thenReturn(1);
        
        PortChecker portChecker = new PortChecker(appConfig);
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            portChecker.customize(factory);
        });
        
        assertTrue(exception.getMessage().contains("All ports from 1 to 1 are in use"));
    }

    @Test
    void testPortCheckerIsSpringComponent() {
        assertTrue(PortChecker.class.isAnnotationPresent(org.springframework.stereotype.Component.class));
    }

    @Test
    void testPortCheckerConstructor() {
        AppConfig appConfig = new AppConfig();
        PortChecker portChecker = new PortChecker(appConfig);
        assertNotNull(portChecker);
    }
}