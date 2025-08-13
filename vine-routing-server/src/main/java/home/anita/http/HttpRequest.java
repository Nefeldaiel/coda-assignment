package home.anita.http;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

/**
 * Base class for HTTP request models.
 * Contains common properties needed for HTTP requests.
 */
public abstract class HttpRequest {

    private final String url;
    private final HttpMethod method;
    private final HttpHeaders headers;
    private final String body;

    protected HttpRequest(String url, HttpMethod method, HttpHeaders headers, String body) {
        this.url = url;
        this.method = method;
        this.headers = headers;
        this.body = body;
    }

    protected HttpRequest(String url, HttpMethod method) {
        this(url, method, new HttpHeaders(), null);
    }

    public String getUrl() {
        return url;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return String.format("%s{method=%s, url='%s', hasHeaders=%s, hasBody=%s}",
                getClass().getSimpleName(),
                method,
                url,
                headers != null && !headers.isEmpty(),
                body != null && !body.isEmpty());
    }
}