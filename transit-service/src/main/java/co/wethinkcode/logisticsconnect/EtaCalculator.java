package co.wethinkcode.logisticsconnect;

/**
 * The deliberately simple ETA model: base journey time plus a fixed penalty per delay stage.
 * This is not a real logistics forecast because the source data has no distances or route times.
 */
final class EtaCalculator {
    static final double BASE_HOURS = 2.0;
    static final double HOURS_PER_STAGE = 1.5;

    private EtaCalculator() {}

    static double estimatedHours(int delayStage) {
        return roundToOneDecimal(BASE_HOURS + delayStage * HOURS_PER_STAGE);
    }

    static double roundToOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    static String message(double hours) {
        return "Estimated arrival in " + hours + " hours";
    }
}