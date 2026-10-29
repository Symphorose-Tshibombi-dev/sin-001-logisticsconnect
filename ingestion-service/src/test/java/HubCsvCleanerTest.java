import co.wethinkcode.logisticsconnect.HubCsvCleaner;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class HubCsvCleanerTest {
    @Test void normalizesWhitespaceCasingProvinceAndFlags() throws Exception {
        String csv = "hub_id, Province ,sorting_center,active\nh-1,gauteng,Johannesburg  Central,Y\n";
        var hubs = HubCsvCleaner.clean(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));
        assertEquals(1, hubs.size());
        assertEquals("H-1", hubs.get(0).hubId());
        assertEquals("Gauteng", hubs.get(0).province());
        assertEquals("Johannesburg Central", hubs.get(0).sortingCenter());
        assertEquals(Boolean.TRUE, hubs.get(0).active());
    }

}
