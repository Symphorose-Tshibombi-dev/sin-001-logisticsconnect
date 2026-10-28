import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class HubLookupTest {
    private static Map<String, Object> hub(String id, String province, String centre, Boolean active) {
        Map<String, Object> m = new HashMap<>();
        m.put("hub_id", id);
        m.put("province", province);
        m.put("sorting_center", centre);
        m.put("active", active);
        return m;
    }

    private final List<Map<String, Object>> hubs = List.of(
            hub("H-500", "Gauteng", "Johannesburg Central", true),
            hub("H-501", "Western Cape", "Cape Town Port", true));

    @Test void findsHubById() {
        var found = HubLookup.findById(hubs, "H-501");
        assertTrue(found.isPresent());
        assertEquals("Cape Town Port", found.get().get("sorting_center"));
    }

    @Test void matchIgnoresCaseAndSurroundingSpaces() {
        assertTrue(HubLookup.findById(hubs, "h-500").isPresent());
        assertTrue(HubLookup.findById(hubs, " H-500 ").isPresent());
    }

    @Test void unknownHubIsEmpty() {
        assertTrue(HubLookup.findById(hubs, "H-999").isEmpty());
    }

    @Test void handlesNullInputsAndNullEntries() {
        assertTrue(HubLookup.findById(null, "H-500").isEmpty());
        assertTrue(HubLookup.findById(hubs, null).isEmpty());
        var withNull = new java.util.ArrayList<Map<String, Object>>();
        withNull.add(null);
        withNull.add(hub("H-500", "Gauteng", "Johannesburg Central", true));
        assertTrue(HubLookup.findById(withNull, "H-500").isPresent());
    }

    @Test void keepsNullActiveFlagInTheResult() {
        var found = HubLookup.findById(List.of(hub("H-511", "Limpopo", "Polokwane Hub", null)), "H-511");
        assertTrue(found.isPresent());
        assertNull(found.get().get("active"));
    }
}
