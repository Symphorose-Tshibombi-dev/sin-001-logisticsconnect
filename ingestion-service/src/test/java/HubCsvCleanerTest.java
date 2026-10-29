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

    @Test void mergesDuplicateEntitiesKeepingFirstIdAndTrueIfAnyDuplicateIsTrue() throws Exception {
        String csv = "hub_id,province,sorting_center,active\nH-1,Gauteng,Johannesburg Central,N\nh-2,gauteng,johannesburg central,yes\n";
        var hubs = HubCsvCleaner.clean(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));
        assertEquals(1, hubs.size());
        assertEquals("H-1", hubs.get(0).hubId());
        assertEquals(Boolean.TRUE, hubs.get(0).active());
    }

    @Test void turnsMissingProvinceIntoUnknownAndUnknownFlagIntoNull() throws Exception {
        String csv = "hub_id,province,sorting_center,active\nH-1,,Pretoria North,unknown\n";
        var hubs = HubCsvCleaner.clean(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));
        assertEquals("Unknown", hubs.get(0).province());
        assertNull(hubs.get(0).active());
    }

    private static List<HubRecord> clean(String csv) throws Exception {
        return HubCsvCleaner.clean(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));
    }

    @Test void mergesMissingProvinceRecordIntoTheOneMatchingCentre() throws Exception {
        String csv = "hub_id,province,sorting_center,active\n"
                + "H-1,Gauteng,Pretoria North,0\n"
                + "H-2,,Pretoria North,yes\n";
        var hubs = clean(csv);
        assertEquals(1, hubs.size());
        assertEquals("H-1", hubs.get(0).hubId());
        assertEquals("Gauteng", hubs.get(0).province());
        assertEquals(Boolean.TRUE, hubs.get(0).active());
    }

    @Test void mergesMissingProvinceRecordEvenWhenItAppearsFirst() throws Exception {
        String csv = "hub_id,province,sorting_center,active\n"
                + "H-2,,Pretoria North,yes\n"
                + "H-1,Gauteng,Pretoria North,0\n";
        var hubs = clean(csv);
        assertEquals(1, hubs.size());
        assertEquals("H-1", hubs.get(0).hubId());
        assertEquals("Gauteng", hubs.get(0).province());
    }

    @Test void doesNotGuessWhenMissingProvinceMatchesCentresInSeveralProvinces() throws Exception {
        String csv = "hub_id,province,sorting_center,active\n"
                + "H-1,Gauteng,Central Hub,Y\n"
                + "H-2,Western Cape,Central Hub,Y\n"
                + "H-3,,Central Hub,Y\n";
        var hubs = clean(csv);
        assertEquals(3, hubs.size());
        assertTrue(hubs.stream().anyMatch(h -> h.hubId().equals("H-3") && h.province().equals("Unknown")));
    }

    @Test void keepsMissingProvinceRecordWhenNothingMatches() throws Exception {
        var hubs = clean("hub_id,province,sorting_center,active\nH-1,,Lonely Hub,Y\n");
        assertEquals(1, hubs.size());
        assertEquals("Unknown", hubs.get(0).province());
    }

    @Test void dropsRowsWithPlaceholderHubIdOrCentre() throws Exception {
        String csv = "hub_id,province,sorting_center,active\n"
                + "N/A,Gauteng,Somewhere,Y\n"
                + "H-1,Gauteng,TBD,Y\n"
                + "H-2,Gauteng,Real Hub,Y\n";
        var hubs = clean(csv);
        assertEquals(1, hubs.size());
        assertEquals("H-2", hubs.get(0).hubId());
    }

    @Test void unifiesProvinceSpellingVariants() throws Exception {
        String csv = "hub_id,province,sorting_center,active\n"
                + "H-1,Kwa-Zulu Natal,Durban Harbour,1\n"
                + "H-2,KwaZulu Natal,Durban Harbour,true\n";
        var hubs = clean(csv);
        assertEquals(1, hubs.size());
        assertEquals("KwaZulu-Natal", hubs.get(0).province());
    }

    @Test void mergeActiveTruthTable() {
        assertEquals(Boolean.TRUE, HubCsvCleaner.mergeActive(Boolean.TRUE, Boolean.FALSE));
        assertEquals(Boolean.TRUE, HubCsvCleaner.mergeActive(null, Boolean.TRUE));
        assertEquals(Boolean.FALSE, HubCsvCleaner.mergeActive(Boolean.FALSE, null));
        assertEquals(Boolean.FALSE, HubCsvCleaner.mergeActive(Boolean.FALSE, Boolean.FALSE));
        assertNull(HubCsvCleaner.mergeActive(null, null));
    }

    @Test void bundledCsvCleansToTenUniqueHubsWithNoUnknownProvince() {
        var hubs = HubCsvCleaner.loadDefault();
        assertEquals(10, hubs.size());
        assertTrue(hubs.stream().noneMatch(h -> h.province().equals("Unknown")));
        assertEquals(hubs.size(), hubs.stream().map(HubRecord::hubId).distinct().count());
        // Johannesburg Central appears under H-500, H-504, H-510 and H-515 in the raw file
        assertEquals(1, hubs.stream().filter(h -> h.sortingCenter().equals("Johannesburg Central")).count());
        // Pretoria North appears under H-502 and H-508 (the latter with a blank province)
        assertEquals(1, hubs.stream().filter(h -> h.sortingCenter().equals("Pretoria North")).count());
    }
}
