package co.wethinkcode.logisticsconnect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransitEtaTest {
    @Test
    void stageZeroUsesBaseJourneyTime() {
        assertEquals(2.0, EtaCalculator.estimatedHours(0));
    }

    @Test
    void knownStagesFollowTheEstimateFormula() {
        assertEquals(6.5, EtaCalculator.estimatedHours(3));
        assertEquals(14.0, EtaCalculator.estimatedHours(8));
    }

    @Test
    void estimatedTimeIncreasesWithEachDelayStage() {
        double previous = EtaCalculator.estimatedHours(0);
        for (int stage = 1; stage <= 8; stage++) {
            double current = EtaCalculator.estimatedHours(stage);
            assertTrue(current > previous, "Expected stage " + stage + " to increase ETA");
            previous = current;
        }
    }

    @Test
    void roundsToOneDecimalPlace() {
        assertEquals(2.3, EtaCalculator.roundToOneDecimal(2.26));
        assertEquals(2.2, EtaCalculator.roundToOneDecimal(2.18));
    }

    @Test
    void messageIncludesEstimatedHours() {
        assertEquals("Estimated arrival in 6.5 hours", EtaCalculator.message(6.5));
    }

    @Test
    void rejectsDelayStagesOutsideSupportedRange() {
        assertThrows(IllegalArgumentException.class, () -> EtaCalculator.estimatedHours(-1));
        assertThrows(IllegalArgumentException.class, () -> EtaCalculator.estimatedHours(9));
    }
}
