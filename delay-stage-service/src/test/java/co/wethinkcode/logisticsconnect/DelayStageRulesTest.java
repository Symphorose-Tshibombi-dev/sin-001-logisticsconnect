package co.wethinkcode.logisticsconnect;

import org.junit.jupiter.api.Test;
import java.util.OptionalInt;
import static org.junit.jupiter.api.Assertions.*;

class DelayStageRulesTest {
    @Test void acceptsEveryStageFromZeroThroughEight() {
        for (int stage = 0; stage <= 8; stage++) {
            assertEquals(OptionalInt.of(stage), DelayStageRules.parseStage(stage), "stage " + stage);
        }
    }

    @Test void rejectsStagesOutsideTheRange() {
        assertTrue(DelayStageRules.parseStage(-1).isEmpty());
        assertTrue(DelayStageRules.parseStage(9).isEmpty());
        assertTrue(DelayStageRules.parseStage(Long.MAX_VALUE).isEmpty());
    }

    @Test void rejectsNonWholeAndNonNumericValues() {
        assertTrue(DelayStageRules.parseStage(3.5).isEmpty());
        assertTrue(DelayStageRules.parseStage("3").isEmpty());
        assertTrue(DelayStageRules.parseStage(null).isEmpty());
        assertTrue(DelayStageRules.parseStage(Boolean.TRUE).isEmpty());
    }

    @Test void acceptsWholeNumbersWrittenAsDecimals() {
        assertEquals(OptionalInt.of(3), DelayStageRules.parseStage(3.0));
    }

    @Test void hubIdsAreTrimmedAndUppercased() {
        assertEquals("H-500", DelayStageRules.normalizeHubId("  h-500 "));
        assertEquals("", DelayStageRules.normalizeHubId(null));
    }
}