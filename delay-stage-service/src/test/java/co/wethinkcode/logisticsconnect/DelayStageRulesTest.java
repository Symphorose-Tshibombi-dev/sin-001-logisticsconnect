package co.wethinkcode.logisticsconnect;

import org.junit.jupiter.api.Test;
import java.util.OptionalInt;
import static org.junit.jupiter.api.Assertions.*;

class DelayStageRulesTest {
    @Test void acceptsEverySupportedStage() {
        for (int stage = 0; stage <= 8; stage++) assertEquals(OptionalInt.of(stage), DelayStageRules.parseStage(stage));
    }
    @Test void rejectsValuesOutsideSupportedRange() {
        assertTrue(DelayStageRules.parseStage(-1).isEmpty());
        assertTrue(DelayStageRules.parseStage(9).isEmpty());
        assertTrue(DelayStageRules.parseStage(Long.MAX_VALUE).isEmpty());
    }
    @Test void rejectsFractionsAndNonNumericValues() {
        assertTrue(DelayStageRules.parseStage(3.5).isEmpty());
        assertTrue(DelayStageRules.parseStage("3").isEmpty());
        assertTrue(DelayStageRules.parseStage(null).isEmpty());
        assertTrue(DelayStageRules.parseStage(Boolean.TRUE).isEmpty());
    }
    @Test void acceptsWholeNumbersRepresentedAsDecimals() {
        assertEquals(OptionalInt.of(3), DelayStageRules.parseStage(3.0));
    }
    @Test void normalizesHubIdsAndHandlesNull() {
        assertEquals("H-500", DelayStageRules.normalizeHubId("  h-500 "));
        assertEquals("", DelayStageRules.normalizeHubId(null));
    }
}
