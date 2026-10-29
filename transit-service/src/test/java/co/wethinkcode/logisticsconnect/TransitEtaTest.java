package co.wethinkcode.logisticsconnect;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TransitEtaTest {
    @Test void stageZeroIsJustTheBaseTime() {
        assertEquals(2.0, EtaCalculator.estimatedHours(0));
    }

    @Test void knownStagesMatchTheFormula() {
        assertEquals(6.5, EtaCalculator.estimatedHours(3));
        assertEquals(14.0, EtaCalculator.estimatedHours(8));
    }

    @Test void etaNeverDecreasesAsDelayStageRises() {
        double old = EtaCalculator.estimatedHours(0);
        for (int stage = 1; stage <= 8; stage++) {
            double present = EtaCalculator.estimatedHours(stage);
            assertTrue(present > old, "stage " + stage);
            old = present;
        }
    }

    @Test void roundsToOneDecimalPlace() {
        assertEquals(2.3, EtaCalculator.roundToOneDecimal(2.26));
        assertEquals(2.2, EtaCalculator.roundToOneDecimal(2.18));
    }

    @Test void messageMentionsTheHours() {
        assertEquals("Estimated arrival in 6.5 hours", EtaCalculator.message(6.5));
    }
}