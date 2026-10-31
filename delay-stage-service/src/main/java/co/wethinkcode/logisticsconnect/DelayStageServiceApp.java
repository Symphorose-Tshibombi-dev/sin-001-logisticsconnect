package co.wethinkcode.logisticsconnect;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import java.util.Map;
import java.util.OptionalInt;

public class DelayStageServiceApp {
    private static final DelayStageStore STORE = new DelayStageStore();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> config.http.defaultContentType = "application/json");
        app.get("/health", ctx -> ctx.result("OK"));
        app.get("/delay-stage/{hubId}", ctx -> {
            String id = DelayStageRules.normalizeHubId(ctx.pathParam("hubId"));
            if (id.isBlank()) {
                ctx.status(400).json(Map.of("error", "hubId is required"));
                return;
            }
            ctx.json(Map.of("hubId", id, "stage", STORE.get(id)));
        });
        app.post("/delay-stage/{hubId}", ctx -> {
            String id = DelayStageRules.normalizeHubId(ctx.pathParam("hubId"));
            if (id.isBlank()) {
                ctx.status(400).json(Map.of("error", "hubId is required"));
                return;
            }
            Map<?, ?> body;
            try {
                body = MAPPER.readValue(ctx.body(), Map.class);
            } catch (Exception e) {
                ctx.status(400).json(Map.of("error", "Request body must be valid JSON"));
                return;
            }
            OptionalInt stage = DelayStageRules.parseStage(body == null ? null : body.get("stage"));
            if (stage.isEmpty()) {
                ctx.status(400).json(Map.of("error", "stage must be an integer from 0 to 8"));
                return;
            }
            STORE.set(id, stage.getAsInt());
            ctx.json(Map.of("hubId", id, "stage", stage.getAsInt(), "message", "Delay stage updated"));
        });
        app.start(7052);
    }
}
