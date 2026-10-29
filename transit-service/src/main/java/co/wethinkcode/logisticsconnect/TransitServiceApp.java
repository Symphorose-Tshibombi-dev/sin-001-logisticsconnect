package co.wethinkcode.logisticsconnect;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

public class TransitServiceApp {
    private static final String HUB_URL = System.getProperty("hub.url", "http://localhost:7051");
    private static final String DELAY_URL = System.getProperty("delay.url", "http://localhost:7052");
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();

    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> config.http.defaultContentType = "application/json");
        app.get("/health", ctx -> ctx.result("OK"));
        app.get("/eta/{hubId}", ctx -> {
            String hubId = ctx.pathParam("hubId").trim();
            if (hubId.isEmpty()) {
                ctx.status(400).json(Map.of("error", "Hub ID must not be blank"));
                return;
            }

            try {
                Map<String, Object> hub = getJson(HUB_URL + "/hubs/" + encodePath(hubId));
                Map<String, Object> delay = getJson(DELAY_URL + "/delay-stage/" + encodePath(hubId));
                Object stageValue = delay.get("stage");
                if (!(stageValue instanceof Number)) {
                    ctx.status(503).json(Map.of("error", "Delay service returned an invalid stage"));
                    return;
                }

                int stage = ((Number) stageValue).intValue();
                double hours = EtaCalculator.estimatedHours(stage);
                ctx.json(Map.of(
                        "hubId", hubId.toUpperCase(),
                        "hub", hub,
                        "delayStage", stage,
                        "estimatedHours", hours,
                        "message", EtaCalculator.message(hours)
                ));
            } catch (NotFoundException e) {
                ctx.status(404).json(Map.of("error", "Hub or delay stage not found", "hubId", hubId));
            } catch (IllegalArgumentException e) {
                ctx.status(502).json(Map.of("error", "Delay service returned an unsupported stage"));
            } catch (Exception e) {
                ctx.status(503).json(Map.of("error", "A dependent service is unavailable"));
            }
        });
        app.start(7053);
    }

    private static String encodePath(String value) {
        return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static Map<String, Object> getJson(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(3))
                .GET()
                .build();
        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 404) {
            throw new NotFoundException();
        }
        if (response.statusCode() != 200) {
            throw new IllegalStateException("Downstream status " + response.statusCode());
        }
        return MAPPER.readValue(response.body(), new TypeReference<>() {});
    }

    private static class NotFoundException extends Exception {}
}
