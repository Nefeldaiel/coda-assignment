# Vine Routing Server

A Spring Boot-based HTTP routing server that dispatches POST requests to a configured list of application servers using random selection.

## Features

1. **POST-only routing**: Only accepts HTTP POST requests, returns 405 for other methods
2. **Random dispatch**: Randomly selects an instance from the configured application server list
3. **Error propagation**: Returns the same errors (404, 405, 5xx) as the downstream application servers
4. **Configurable servers**: Application server list can be configured via `application.yml`

## Configuration

Configure application servers in `src/main/resources/application.yml`:

```yaml
server:
  port: 8080

routing:
  servers:
    - url: "http://localhost:9001"
    - url: "http://localhost:9002" 
    - url: "http://localhost:9003"
```

## Usage

1. **Start the routing server**:
   ```bash
   ./gradlew bootRun
   ```

2. **Send POST requests**:
   ```bash
   curl -X POST http://localhost:8080/api/echo \
        -H "Content-Type: application/json" \
        -d '{"message": "test"}'
   ```

3. **The routing server will**:
   - Randomly select one of the configured application servers
   - Forward the POST request with all headers and body
   - Return the exact response from the selected server

## Error Handling

- **405 Method Not Allowed**: Returned for non-POST requests
- **404/5xx errors**: Propagated from downstream servers
- **500 Internal Server Error**: When no servers are configured or routing fails

## Testing

Run tests with:
```bash
./gradlew test
```

## Integration with Grape Application Server

This routing server is designed to work with the `grape-application-server` module. Start multiple instances of the grape server on different ports and configure them in the routing server's `application.yml`.

Example setup:
1. Start grape-application-server on ports 9001, 9002, 9003
2. Configure routing server to use these URLs
3. Send requests to the routing server, which will distribute them randomly