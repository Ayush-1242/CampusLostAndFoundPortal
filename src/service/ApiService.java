package service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

public class ApiService {

    private static final String BASE_URL =
            "http://localhost:8080/CampusLostAndFound";

    private static final CookieManager cookieManager =
            new CookieManager();

    private static final HttpClient client;

    static {
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

        client = HttpClient.newBuilder()
                .cookieHandler(cookieManager)
                .build();
    }

    // GET request
    public static String get(String endpoint) throws Exception {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .GET()
                .header("Accept", "application/json")
                .build();

        return send(request);
    }

    // POST request using form parameters
    public static String post(String endpoint,
                              Map<String, String> parameters)
            throws Exception {

        String formData = parameters.entrySet()
                .stream()
                .map(entry ->
                        encode(entry.getKey()) + "="
                                + encode(entry.getValue()))
                .collect(Collectors.joining("&"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type",
                        "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .build();

        return send(request);
    }

    // Send request and handle response
    private static String send(HttpRequest request)
            throws Exception {

        HttpResponse<String> response =
                client.send(request,
                        HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            throw new RuntimeException(
                    "HTTP " + response.statusCode()
                            + ": " + response.body());
        }

        return response.body();
    }

    // URL encoding
    private static String encode(String value) {

        return URLEncoder.encode(
                value,
                StandardCharsets.UTF_8);
    }
}
