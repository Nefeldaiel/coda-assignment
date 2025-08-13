package home.anita;

import home.anita.server.HealthAwareServerSelector;
import home.anita.server.HealthCheckService;
import home.anita.server.RandomServerSelector;
import home.anita.server.ServerSelector;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "routing.servers[0].url=http://localhost:9001",
    "routing.servers[1].url=http://localhost:9002"
})
class RoutingConfigurationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void testServerSelectorBeanIsHealthAware() {
        ServerSelector serverSelector = applicationContext.getBean(ServerSelector.class);
        
        assertNotNull(serverSelector);
        assertInstanceOf(HealthAwareServerSelector.class, serverSelector);
    }

    @Test
    void testRandomServerSelectorBeanExists() {
        RandomServerSelector randomServerSelector = applicationContext.getBean(RandomServerSelector.class);
        
        assertNotNull(randomServerSelector);
    }

    @Test
    void testHealthCheckServiceBeanExists() {
        HealthCheckService healthCheckService = applicationContext.getBean(HealthCheckService.class);
        
        assertNotNull(healthCheckService);
    }

    @Test
    void testAllRequiredBeansArePresent() {
        // Verify all components are properly wired
        assertNotNull(applicationContext.getBean(RoutingService.class));
        assertNotNull(applicationContext.getBean(RoutingController.class));
        assertNotNull(applicationContext.getBean(HeaderHandler.class));
        assertNotNull(applicationContext.getBean(RoutingConfig.class));
        assertNotNull(applicationContext.getBean(ServerSelector.class));
        assertNotNull(applicationContext.getBean(RandomServerSelector.class));
        assertNotNull(applicationContext.getBean(HealthCheckService.class));
    }
}