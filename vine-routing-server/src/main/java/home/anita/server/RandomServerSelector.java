package home.anita.server;

import home.anita.RoutingConfig.ServerConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.Set;

/**
 * Implementation of ServerSelector that randomly selects a server from the available set.
 */
@Component
@Slf4j
public class RandomServerSelector implements ServerSelector {

    private final Random random = new Random();

    /**
     * Randomly selects a server from the given set of servers.
     *
     * @param servers The set of available servers to select from
     * @return A randomly selected server configuration
     * @throws IllegalArgumentException if the server set is null or empty
     */
    @Override
    public ServerConfig select(Set<ServerConfig> servers) {
        if (servers == null || servers.isEmpty()) {
            throw new IllegalArgumentException("Server set cannot be null or empty");
        }

        int size = servers.size();
        int index = random.nextInt(size);

        int currentIndex = 0;
        for (ServerConfig server : servers) {
            if (currentIndex == index) {
                log.debug("Selected server {}/{}: {}", index + 1, size, server.getUrl());
                return server;
            }
            currentIndex++;
        }

        // This should never happen, but as a fallback return the first server
        ServerConfig fallback = servers.iterator().next();
        log.warn("Fallback to first server due to unexpected selection error: {}", fallback.getUrl());
        return fallback;
    }
}