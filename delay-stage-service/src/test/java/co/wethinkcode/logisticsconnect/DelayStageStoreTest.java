package co.wethinkcode.logisticsconnect;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DelayStageStoreTest {
    @Test void unsetHubsDefaultToStageZero() {
        assertEquals(0, new DelayStageStore().get("H-999"));
    }

    @Test void storedStageIsReturnedAndHubIdIsCaseInsensitive() {
        DelayStageStore store = new DelayStageStore();
        store.set("h-500", 3);
        assertEquals(3, store.get("H-500"));
        assertEquals(3, store.get(" h-500 "));
    }

    @Test void laterUpdateReplacesEarlierOne() {
        DelayStageStore store = new DelayStageStore();
        store.set("H-500", 3);
        store.set("H-500", 7);
        assertEquals(7, store.get("H-500"));
    }

    @Test void hubsAreIndependent() {
        DelayStageStore store = new DelayStageStore();
        store.set("H-500", 5);
        assertEquals(0, store.get("H-501"));
    }

    @Test void refusesInvalidStage() {
        DelayStageStore store = new DelayStageStore();
        assertThrows(IllegalArgumentException.class, () -> store.set("H-500", 9));
        assertEquals(0, store.get("H-500"));
    }
}