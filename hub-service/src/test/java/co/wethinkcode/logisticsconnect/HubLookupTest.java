package co.wethinkcode.logisticsconnect;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HubLookupTest {

    @Test
    void findsHubById() {
        Map<String, Object> hub = Map.of("hub_id", "JHB-01", "province", "Gauteng");
        assertEquals(hub, HubLookup.findById(List.of(hub), "JHB-01").orElseThrow());
    }
}
