package httpclient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.server.Server;

import httpclient.annotation.method.GET;
import httpclient.annotation.method.POST;
import httpclient.annotation.method.PUT;
import httpclient.annotation.parameter.Body;
import httpclient.annotation.parameter.Header;
import httpclient.annotation.parameter.Query;

class ClientFactoryTest {

    static class Dto {
        Integer put;

        Dto(Integer put) {
            this.put = put;
        }
    }

    interface TestClient {

        @GET("/get")
        String get(@Query("query") String query);

        @POST("/post")
        List<String> post(@Body(String.class) String body);

        @PUT("/put")
        Dto put(@Header("header") String header);
    }

    private static Server server;
    private static TestClient client;

    @BeforeAll
    static void init() {
        server = Server.builder()
                       .http(8080)
                       .service("/get", (ctx, req) -> HttpResponse.of("get"))
                       .service("/post", (ctx, req) -> HttpResponse.of("[\"post\"]"))
                       .service("/put", (ctx, req) -> HttpResponse.of("{\"put\":0}"))
                       .build();

        server.closeOnJvmShutdown();
        server.start().join();

        final Gson gson = new GsonBuilder().create();
        final ClientFactory factory = new ClientFactory("http://localhost:8080", gson);
        client = factory.create(TestClient.class);
    }

    @AfterAll
    static void end() {
        server.stop();
    }

    @Test
    @DisplayName("GET 테스트")
    void get() {
        // given

        // when
        final String result = client.get("get");

        // then
        assertEquals(result, "get");
    }

    @Test
    @DisplayName("POST 테스트")
    void post() {

        // when
        final List<String> result = client.post("post");

        // then
        assertIterableEquals(result, Collections.singletonList("post"));
    }

    @Test
    @DisplayName("PUT 테스트")
    void put() {

        // when
        final Dto result = client.put("put");

        // then
        assertEquals(result.put, 0);
    }
}