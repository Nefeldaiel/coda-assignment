package home.anita;

import home.anita.RoutingConfig.ServerConfig;

import java.util.Set;

/**
 * Interface for server selection strategies.
 * Implementations define how to select a server from the available server list.
 */
public interface ServerSelector {
    
    /**
     * Selects a server from the given set of available servers.
     * 
     * @param servers The set of available servers to select from
     * @return The selected server configuration
     * @throws IllegalArgumentException if the server set is null or empty
     */
    ServerConfig selectServer(Set<ServerConfig> servers);
}