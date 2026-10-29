package co.wethinkcode.logisticsconnect;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class HubCsvCleaner {
    private static final Logger LOG = LoggerFactory.getLogger(HubCsvCleaner.class);
    static final String UNKNOWN_PROVINCE = "Unknown";

    private HubCsvCleaner() {}

    public static List<HubRecord> loadDefault() {
        InputStream stream = HubCsvCleaner.class.getResourceAsStream("/hubs-global.csv");
        if (stream == null) throw new IllegalStateException("Could not find hubs-global.csv in resources");
        try (stream) {
            return clean(stream);
        } catch (IOException | CsvException e) {
            throw new IllegalStateException("Could not read hubs-global.csv", e);
        }
    }

    public static List<HubRecord> clean(InputStream input) throws IOException, CsvException {
        try (CSVReader reader = new CSVReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            List<String[]> rows = reader.readAll();
            if (rows.isEmpty()) return List.of();
            String[] headers = rows.get(0);
            Map<String, Integer> indexes = new LinkedHashMap<>();
            for (int i = 0; i < headers.length; i++) {
                indexes.put(normalizeHeader(headers[i]), i);
            }
            List<HubRecord> cleaned = new ArrayList<>();
            Map<String, HubRecord> uniqueByEntity = new LinkedHashMap<>();
            for (int rowIndex = 1; rowIndex < rows.size(); rowIndex++) {
                String[] row = rows.get(rowIndex);
                String id = value(row, indexes, "hub_id").toUpperCase(Locale.ROOT);
                if (id.isBlank() || isPlaceholder(id)) {
                    LOG.warn("Dropping CSV row {}: missing or placeholder hub_id", rowIndex + 1);
                    continue;
                }
                String province = normalizeProvince(value(row, indexes, "province"));
                String center = titleWords(value(row, indexes, "sorting_center"));
                if (isPlaceholder(center)) {
                    LOG.warn("Dropping CSV row {} ({}): missing or placeholder sorting_center", rowIndex + 1, id);
                    continue;
                }
                Boolean active = normalizeBoolean(value(row, indexes, "active"));
                HubRecord candidate = new HubRecord(id, province, center, active);
                String key = province.toLowerCase(Locale.ROOT) + "|" + center.toLowerCase(Locale.ROOT);
                HubRecord existing = uniqueByEntity.get(key);
                if (existing == null) {
                    uniqueByEntity.put(key, candidate);
                } else {
                    // Duplicate policy: keep the first canonical ID and merge the active flags
                    // (see mergeActive for the rule).
                    Boolean mergedActive = mergeActive(existing.active(), candidate.active());
                    uniqueByEntity.put(key, new HubRecord(existing.hubId(), existing.province(),
                            existing.sortingCenter(), mergedActive));
                }
            }
            cleaned.addAll(resolveUnknownProvinces(uniqueByEntity.values()));
            return List.copyOf(cleaned);
        }
    }

    /**
     * Active-flag merge rule for duplicates: {@code true} wins if any duplicate is active,
     * otherwise {@code false} wins if any duplicate is explicitly inactive, otherwise unknown ({@code null}).
     */
    static Boolean mergeActive(Boolean a, Boolean b) {
        if (Boolean.TRUE.equals(a) || Boolean.TRUE.equals(b)) return Boolean.TRUE;
        if (Boolean.FALSE.equals(a) || Boolean.FALSE.equals(b)) return Boolean.FALSE;
        return null;
    }

    /**
     * Second duplicate pass. A record whose province was missing ("Unknown") can never match on the
     * province + centre key, so it is compared on sorting-centre name alone. It is merged into another
     * record only if exactly ONE record with a known province has that centre name; if the match is
     * ambiguous (the same centre name exists in several provinces) it is left alone rather than guessed.
     * The record with the known province keeps its ID and province.
     */
    static List<HubRecord> resolveUnknownProvinces(java.util.Collection<HubRecord> records) {
        List<HubRecord> result = new ArrayList<>(records);
        for (HubRecord unknown : records) {
            if (!UNKNOWN_PROVINCE.equals(unknown.province())) continue;
            List<HubRecord> matches = result.stream()
                    .filter(r -> !UNKNOWN_PROVINCE.equals(r.province()))
                    .filter(r -> r.sortingCenter().equalsIgnoreCase(unknown.sortingCenter()))
                    .toList();
            if (matches.size() != 1) {
                LOG.warn("Hub {} has no province and {} possible matches for '{}'; keeping it as Unknown",
                        unknown.hubId(), matches.size(), unknown.sortingCenter());
                continue;
            }
            HubRecord target = matches.get(0);
            LOG.info("Merging hub {} (no province) into {} - same sorting centre '{}'",
                    unknown.hubId(), target.hubId(), target.sortingCenter());
            result.set(result.indexOf(target), new HubRecord(target.hubId(), target.province(),
                    target.sortingCenter(), mergeActive(target.active(), unknown.active())));
            result.remove(unknown);
        }
        return result;
    }

    private static String value(String[] row, Map<String, Integer> indexes, String name) {
        Integer index = indexes.get(name);
        if (index == null || index >= row.length || row[index] == null) return "";
        return row[index].trim().replaceAll("\\s+", " ");
    }

    private static String normalizeHeader(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", "_");
    }

    private static boolean isPlaceholder(String value) {
        if (value == null || value.isBlank()) return true;
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "n/a", "na", "unknown", "tbd", "-", "nan", "null" -> true;
            default -> false;
        };
    }

    private static String normalizeProvince(String value) {
        if (isPlaceholder(value)) return UNKNOWN_PROVINCE;
        String normalized = titleWords(value);
        String key = normalized.toLowerCase(Locale.ROOT).replaceAll("[\\s-]+", "");
        return switch (key) {
            case "kwazulunatal" -> "KwaZulu-Natal";
            case "easterncape" -> "Eastern Cape";
            case "westerncape" -> "Western Cape";
            case "northwest" -> "North West";
            case "freestate" -> "Free State";
            case "northerncape" -> "Northern Cape";
            default -> normalized;
        };
    }

    private static String titleWords(String value) {
        if (isPlaceholder(value)) return "";
        String[] words = value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT).split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.isBlank()) continue;
            if (result.length() > 0) result.append(' ');
            result.append(word.substring(0, 1).toUpperCase(Locale.ROOT)).append(word.substring(1));
        }
        return result.toString();
    }

    private static Boolean normalizeBoolean(String value) {
        if (value == null) return null;
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "y", "yes", "1", "true", "active" -> Boolean.TRUE;
            case "n", "no", "0", "false", "inactive" -> Boolean.FALSE;
            default -> null;
        };
    }
}