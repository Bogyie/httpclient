package httpclient;

import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import httpclient.annotation.method.DELETE;
import httpclient.annotation.method.GET;
import httpclient.annotation.method.PATCH;
import httpclient.annotation.method.POST;
import httpclient.annotation.method.PUT;
import httpclient.annotation.parameter.Body;
import httpclient.annotation.parameter.Header;
import httpclient.annotation.parameter.Query;
import httpclient.http.HttpMethod;
import httpclient.http.HttpRequest;
import httpclient.http.HttpResponse;

public class ClientFactory {

    private final String baseUrl;
    private final Gson gson;

    public ClientFactory(String baseUrl, Gson gson) {
        this.baseUrl = baseUrl;
        this.gson = gson;
    }

    @SuppressWarnings("unchecked")
    public <T> T create(final Class<T> service) {
        return (T) Proxy.newProxyInstance(
                service.getClassLoader(),
                new Class[] { service },
                (proxy, method, args) -> {

                    final String url;
                    final HttpMethod httpMethod;
                    if (method.isAnnotationPresent(GET.class)) {
                        final GET get = method.getAnnotation(GET.class);
                        url = baseUrl + get.value();
                        httpMethod = HttpMethod.GET;
                    } else if (method.isAnnotationPresent(POST.class)) {
                        final POST post = method.getAnnotation(POST.class);
                        url = baseUrl + post.value();
                        httpMethod = HttpMethod.POST;
                    } else if (method.isAnnotationPresent(PUT.class)) {
                        final PUT put = method.getAnnotation(PUT.class);
                        url = baseUrl + put.value();
                        httpMethod = HttpMethod.PUT;
                    } else if (method.isAnnotationPresent(DELETE.class)) {
                        final DELETE delete = method.getAnnotation(DELETE.class);
                        url = baseUrl + delete.value();
                        httpMethod = HttpMethod.DELETE;
                    } else if (method.isAnnotationPresent(PATCH.class)) {
                        final PATCH patch = method.getAnnotation(PATCH.class);
                        url = baseUrl + patch.value();
                        httpMethod = HttpMethod.PATCH;
                    } else {
                        url = baseUrl;
                        httpMethod = HttpMethod.GET;
                    }

                    String body = "";
                    final Map<String, String> header = new HashMap<>();
                    final Map<String, String> query = new HashMap<>();

                    final Parameter[] parameters = method.getParameters();
                    for (int i = 0; i < parameters.length; i++) {

                        final Parameter parameter = parameters[i];
                        final Object object = args[i];

                        if (parameter.isAnnotationPresent(Body.class)) {
                            final Body b = parameter.getAnnotation(Body.class);
                            body = gson.toJson(object, b.value());
                            continue;
                        }

                        if (parameter.isAnnotationPresent(Header.class)) {
                            final Header h = parameter.getAnnotation(Header.class);
                            header.put(h.value(), String.valueOf(object));
                            continue;
                        }
                        if (parameter.isAnnotationPresent(Query.class)) {
                            final Query q = parameter.getAnnotation(Query.class);
                            query.put(q.value(), String.valueOf(object));
                            continue;
                        }
                    }

                    final HttpRequest request = HttpRequest.builder()
                                                           .url(url)
                                                           .method(httpMethod)
                                                           .query(query)
                                                           .header(header)
                                                           .body(body)
                                                           .build();

                    final HttpResponse response = request.execute();
                    return gson.fromJson(response.getData(), method.getReturnType());
                }
        );
    }
}
