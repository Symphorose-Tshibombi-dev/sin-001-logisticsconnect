package co.wethinkcode.logisticsconnect;

import java.util.OptionalInt;

/** Validation rules for delay stages, kept separate from the HTTP layer so they can be unit tested. */
final class DelayStageRules {
    static final int MIN_STAGE = 0;
    static final int MAX_STAGE = 8;

    private DelayStageRules() {}

    static boolean isValidStage(int stage) {
        return stage >= MIN_STAGE && stage <= MAX_STAGE;
    }

    /** Returns empty unless the value is a whole number between 0 and 8. */
    static OptionalInt parseStage(Object raw) {
        if (!(raw instanceof Number number)) {
            return OptionalInt.empty();
        }
        double numericValue = number.doubleValue();
        if (!Double.isFinite(numericValue) || numericValue != Math.rint(numericValue)
                || numericValue < MIN_STAGE || numericValue > MAX_STAGE) {
            return OptionalInt.empty();
        }
        return OptionalInt.of((int) numericValue);
    }

    static String normalizeHubId(String hubId) {
        return hubId == null ? "" : hubId.trim().toUpperCase(java.util.Locale.ROOT);
    }
}
