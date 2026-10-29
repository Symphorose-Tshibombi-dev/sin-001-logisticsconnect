package co.wethinkcode.logisticsconnect;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HubCsvCleanerTest {
    private List<HubRecord> clean(String csv) throws Exception {
        return HubCsvCleaner.clean(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void trimsFieldsNormalizesNamesAndBooleanFlags() throws Exception {
        List<HubRecord> hubs = clean("hub_id,province,sorting_center,active\n h-502 ,gauteng,Pretoria  North,0\n");
        assertEquals(1, hubs.size());
        assertEquals(new HubRecord("H-502", "Gauteng", "Pretoria North", false), hubs.get(0));
    }

    @Test
    void dropsRowsWithMissingIdsOrSortingCenters() throws Exception {
        List<HubRecord> hubs = clean("hub_id,province,sorting_center,active\n, Gauteng, Pretoria North,yes\nH-2,Gauteng,N/A,yes\nH-3,Gauteng,Durban Central,yes\n");
        assertEquals(1, hubs.size());
        assertEquals("H-3", hubs.get(0).hubId());
    }

    @Test
    void mergesDuplicateCenterRecordsAndLetsTrueWin() throws Exception {
        List<HubRecord> hubs = clean("hub_id,province,sorting_center,active\nH-1,Gauteng,Johannesburg Central,no\nh-2,gauteng,JOHANNESBURG CENTRAL,yes\n");
        assertEquals(1, hubs.size());
        assertEquals("H-1", hubs.get(0).hubId());
        assertEquals(Boolean.TRUE, hubs.get(0).active());
    }

    @Test
    void keepsUnknownProvinceWhenMatchingKnownCenterIsAmbiguous() throws Exception {
        List<HubRecord> hubs = clean("hub_id,province,sorting_center,active\nH-1,Gauteng,Central Hub,yes\nH-2,Western Cape,Central Hub,no\nH-3,N/A,Central Hub,yes\n");
        assertEquals(3, hubs.size());
        assertTrue(hubs.stream().anyMatch(hub -> hub.hubId().equals("H-3") && hub.province().equals("Unknown")));
    }

    @Test
    void returnsEmptyListForHeaderOnlyCsv() throws Exception {
        assertTrue(clean("hub_id,province,sorting_center,active\n").isEmpty());
    }
}
