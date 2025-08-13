package home.anita.server;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Model representing the health status of a server node.
 */
public class ServerHealth {
    
    public enum Status {
        HEALTHY,
        UNHEALTHY
    }
    
    private final String url;
    private Status status;
    private LocalDateTime lastChecked;
    private String errorMessage;
    
    public ServerHealth(String url) {
        this.url = url;
        this.status = Status.UNHEALTHY; // Default to unhealthy until proven otherwise
        this.lastChecked = null;
        this.errorMessage = null;
    }
    
    public String getUrl() {
        return url;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
        this.lastChecked = LocalDateTime.now();
        if (status == Status.HEALTHY) {
            this.errorMessage = null; // Clear error message when healthy
        }
    }
    
    public LocalDateTime getLastChecked() {
        return lastChecked;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public boolean isHealthy() {
        return status == Status.HEALTHY;
    }
    
    public boolean isUnhealthy() {
        return status == Status.UNHEALTHY;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ServerHealth that = (ServerHealth) obj;
        return Objects.equals(url, that.url);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(url);
    }
    
    @Override
    public String toString() {
        return String.format("ServerHealth{url='%s', status=%s, lastChecked=%s, errorMessage='%s'}", 
            url, status, lastChecked, errorMessage);
    }
}