package home.anita.server;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import static home.anita.server.ServerHealth.Status.UNHEALTHY;

/**
 * Model representing the health status of a server node.
 */
@Getter
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ServerHealth {

    public enum Status {
        HEALTHY,
        UNHEALTHY
    }

    @EqualsAndHashCode.Include
    private final String url;
    
    private Status status = UNHEALTHY; // Default to unhealthy until proven otherwise
    private LocalDateTime lastChecked;
    
    @Setter
    private String errorMessage;

    /**
     * Sets the health status and updates the last checked timestamp.
     * Clears error message when status becomes healthy.
     * 
     * @param status The new health status
     */
    public void setStatus(Status status) {
        this.status = status;
        this.lastChecked = LocalDateTime.now();
        if (status == Status.HEALTHY) {
            this.errorMessage = null; // Clear error message when healthy
        }
    }

    /**
     * Checks if the server is currently healthy.
     * 
     * @return true if status is HEALTHY
     */
    public boolean isHealthy() {
        return status == Status.HEALTHY;
    }

    /**
     * Checks if the server is currently unhealthy.
     * 
     * @return true if status is UNHEALTHY
     */
    public boolean isUnhealthy() {
        return status == UNHEALTHY;
    }

    @Override
    public String toString() {
        return String.format("ServerHealth{url='%s', status=%s, lastChecked=%s, errorMessage='%s'}",
                url, status, lastChecked, errorMessage);
    }
}