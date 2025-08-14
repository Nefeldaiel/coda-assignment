package home.anita;

import home.anita.server.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration class for routing components.
 */
@Configuration
public class RoutingConfiguration {

    /**
     * Provides the primary server selector bean.
     * Uses RoundRobinServerSelector as the first priority, which distributes
     * requests evenly across healthy servers in a round-robin manner.
     *
     * @param healthCheckService   The health check service for server status
     * @return The configured round-robin server selector
     */
    @Bean
    @Primary
    public ServerSelector serverSelector(RandomServerSelector randomServerSelector,
                                         HealthCheckService healthCheckService) {
        return new RoundRobinServerSelector(randomServerSelector, healthCheckService);
    }

    /**
     * Provides the health-aware server selector bean.
     * Available as an alternative to round-robin selection if needed.
     *
     * @param randomServerSelector The random server selector for fallback
     * @param healthCheckService   The health check service for server status
     * @return The health-aware server selector
     */
    @Bean("healthAwareServerSelector")
    public HealthAwareServerSelector healthAwareServerSelector(RandomServerSelector randomServerSelector,
                                                               HealthCheckService healthCheckService) {
        return new HealthAwareServerSelector(randomServerSelector, healthCheckService);
    }

    /**
     * Fallback server selector bean if no primary is configured.
     * This ensures the application can still function even if health checking is disabled.
     *
     * @return Random server selector as fallback
     */
    @Bean
    @ConditionalOnMissingBean(ServerSelector.class)
    public ServerSelector fallbackServerSelector() {
        return new RandomServerSelector();
    }

}