package co.wethinkcode.logisticsconnect;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class HubServiceApp {
    private static final String INGESTION_URL = System.getProperty("ingestion.url", "http://localhost:7050");
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();

    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> config.http.defaultContentType = "application/json");
        app.get("/health", ctx -> ctx.result("OK"));
        app.get("/hubs", ctx -> {
            try {
                ctx.json(fetchHubs());
            } catch (Exception e) {
                ctx.status(503).json(Map.of("error", "Ingestion service unavailable"));
            }
        });
        app.get("/hubs/{hubId}", ctx -> {
            try {
                List<Map<String, Object>> hubs = fetchHubs();
                HubLookup.findById(hubs, ctx.pathParam("hubId")).ifPresentOrElse(ctx::json,
                        () -> ctx.status(404).json(Map.of("error", "Hub not found")));
            } catch (Exception e) {
                ctx.status(503).json(Map.of("error", "Ingestion service unavailable"));
            }
        });
        app.start(7051);
    }

    static List<Map<String, Object>> fetchHubs() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(INGESTION_URL + "/hubs"))
                .timeout(Duration.ofSeconds(3)).GET().build();
        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new IllegalStateException("Ingestion returned " + response.statusCode());
        return MAPPER.readValue(response.body(), new TypeReference<>() {});
    }
}
