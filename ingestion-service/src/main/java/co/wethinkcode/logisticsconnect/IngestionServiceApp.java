package co.wethinkcode.logisticsconnect;

import io.javalin.Javalin;

import java.util.Map;

public class IngestionServiceApp {
    public static void main(String[] args) {
        var hubs = HubCsvCleaner.loadDefault();
        Javalin app = Javalin.create(config -> config.http.defaultContentType = "application/json");
        app.get("/health", ctx -> ctx.result("OK"));
        app.get("/hubs", ctx -> ctx.json(hubs.stream().map(HubRecord::toJsonMap).toList()));
        app.get("/hubs/{hubId}", ctx -> hubs.stream()
                .filter(hub -> hub.hubId().equalsIgnoreCase(ctx.pathParam("hubId")))
                .findFirst()
                .ifPresentOrElse(hub -> ctx.json(hub.toJsonMap()),
                        () -> ctx.status(404).json(Map.of("error", "Hub not found"))));
        app.start(7050);
    }
}
