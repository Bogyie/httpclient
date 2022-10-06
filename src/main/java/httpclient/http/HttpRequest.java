package httpclient.http;

import static java.util.Objects.requireNonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public final class HttpRequest {

    private final String url;
    private final HttpMethod method;
    private final Map<String, String> header;
    private final Map<String, String> query;
    private final String body;

    public static HttpRequestBuilder builder() {
        return new HttpRequestBuilder();
    }

    public HttpResponse execute() throws IOException {
        // set query
        final String queryString = query.entrySet()
                                        .stream()
                                        .map(entry -> entry.getKey() + '=' + entry.getValue())
                                        .collect(Collectors.joining("&"));
        final String uri;
        if (!queryString.isEmpty()) {
            uri = url + '?' + queryString;
        } else {
            uri = url;
        }

        final URL url = new URL(uri);
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod(method.name());

        // set headers
        for (Entry<String, String> entry : header.entrySet()) {
            conn.setRequestProperty(entry.getKey(), entry.getValue());
        }

        // body
        if ((method == HttpMethod.POST || method == HttpMethod.PUT) && !body.isEmpty()) {
            try (OutputStream os = conn.getOutputStream()) {
                final byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
                os.write(bytes);
            }
        }

        final int statusCode = conn.getResponseCode();
        final StringBuilder sb = new StringBuilder();
        try (
                InputStream is = isOk(statusCode) ? conn.getInputStream() : conn.getErrorStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is))
        ) {
            String data;
            while ((data = br.readLine()) != null) {
                sb.append(data);
            }
        }

        return HttpResponse.builder()
                           .statusCode(statusCode)
                           .message(conn.getResponseMessage())
                           .contentType(conn.getContentType())
                           .data(sb.toString())
                           .build();
    }

    public HttpRequest(String url,
                        HttpMethod method,
                        Map<String, String> header,
                        Map<String, String> query,
                        String body) {
        this.url = requireNonNull(url, "url");
        this.method = requireNonNull(method, "method");
        this.header = requireNonNull(header, "header");
        this.query = requireNonNull(query, "query");
        this.body = requireNonNull(body, "body");
    }

    private static boolean isOk(int code) {
        return code == HttpURLConnection.HTTP_OK
               || code == HttpURLConnection.HTTP_CREATED
               || code == HttpURLConnection.HTTP_ACCEPTED
               || code == HttpURLConnection.HTTP_NOT_AUTHORITATIVE
               || code == HttpURLConnection.HTTP_NO_CONTENT
               || code == HttpURLConnection.HTTP_RESET
               || code == HttpURLConnection.HTTP_PARTIAL;
    }

    public static final class HttpRequestBuilder {
        private String url;
        private HttpMethod method;
        private Map<String, String> header;
        private Map<String, String> query;
        private String body = "";

        public HttpRequestBuilder url(String url) {
            this.url = url;
            return this;
        }

        public HttpRequestBuilder method(HttpMethod method) {
            this.method = method;
            return this;
        }

        public HttpRequestBuilder header(Map<String, String> header) {
            this.header = header;
            return this;
        }

        public HttpRequestBuilder query(Map<String, String> query) {
            this.query = query;
            return this;
        }

        public HttpRequestBuilder body(String body) {
            this.body = body;
            return this;
        }

        public HttpRequest build() {
            return new HttpRequest(url, method, header, query, body);
        }

        private HttpRequestBuilder() {}
    }
}
