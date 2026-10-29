package co.wethinkcode.logisticsconnect;

import java.util.concurrent.ConcurrentHashMap;

/** In-memory delay stage per hub. Unset hubs are at stage 0. Hub IDs are case-insensitive. */
final class DelayStageStore {
    private final ConcurrentHashMap<String, Integer> stages = new ConcurrentHashMap<>();

    int get(String hubId) {
        return stages.getOrDefault(DelayStageRules.normalizeHubId(hubId), DelayStageRules.MIN_STAGE);
    }

    void set(String hubId, int stage) {
        if (!DelayStageRules.isValidStage(stage)) {
            throw new IllegalArgumentException("stage must be from 0 to 8");
        }
        stages.put(DelayStageRules.normalizeHubId(hubId), stage);
    }
}