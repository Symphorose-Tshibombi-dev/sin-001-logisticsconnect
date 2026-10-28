package co.wethinkcode.logisticsconnect;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Finds a hub in the cleaned list from ingestion-service. Kept separate from HTTP so it can be tested. */
final class HubLookup {
    private HubLookup() {}

    static Optional<Map<String, Object>> findById(List<Map<String, Object>> hubs, String hubId) {
        if (hubs == null || hubId == null) return Optional.empty();
        String wanted = hubId.trim();
        return hubs.stream()
                .filter(h -> h != null && wanted.equalsIgnoreCase(String.valueOf(h.get("hub_id"))))
                .findFirst();
    }
}
