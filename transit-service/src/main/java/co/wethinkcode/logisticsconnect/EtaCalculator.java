package co.wethinkcode.logisticsconnect;

/**
 * A deliberately simple ETA estimate: base journey time plus a fixed penalty per delay stage.
 * It is not a real route forecast because the available data contains no route distances or durations.
 */
final class EtaCalculator {
    static final double BASE_HOURS = 2.0;
    static final double HOURS_PER_STAGE = 1.5;

    private EtaCalculator() {}

    static double estimatedHours(int delayStage) {
        if (delayStage < 0 || delayStage > 8) {
            throw new IllegalArgumentException("Delay stage must be between 0 and 8");
        }
        return roundToOneDecimal(BASE_HOURS + delayStage * HOURS_PER_STAGE);
    }

    static double roundToOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    static String message(double hours) {
        return "Estimated arrival in " + hours + " hours";
    }
}
