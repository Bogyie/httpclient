package httpclient.http;

import static java.util.Objects.requireNonNull;

public final class HttpResponse {

    private final int statusCode;
    private final String message;
    private final String data;
    private final String contentType;

    public static HttpResponseBuilder builder() {
        return new HttpResponseBuilder();
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public String getData() {
        return data;
    }

    public String getContentType() {
        return contentType;
    }

    private HttpResponse(int statusCode, String message, String data, String contentType) {
        this.statusCode = statusCode;
        this.message = requireNonNull(message, "message");
        this.data = data != null ? data : "";
        this.contentType = contentType != null ? contentType : "";
    }

    public static final class HttpResponseBuilder {
        private int statusCode;
        private String message;
        private String data;
        private String contentType;

        public HttpResponseBuilder statusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public HttpResponseBuilder message(String message) {
            this.message = message;
            return this;
        }

        public HttpResponseBuilder data(String data) {
            this.data = data;
            return this;
        }

        public HttpResponseBuilder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public HttpResponse build() {
            return new HttpResponse(statusCode, message, data, contentType);
        }

        private HttpResponseBuilder() {}
    }
}
