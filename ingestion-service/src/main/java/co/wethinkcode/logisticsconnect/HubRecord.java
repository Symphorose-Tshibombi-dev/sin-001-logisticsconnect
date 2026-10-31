package co.wethinkcode.logisticsconnect;

import java.util.LinkedHashMap;
import java.util.Map;

public record HubRecord(String hubId, String province, String sortingCenter, Boolean active) {
    public Map<String, Object> toJsonMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("hub_id", hubId);
        map.put("province", province);
        map.put("sorting_center", sortingCenter);
        map.put("active", active);
        return map;
    }
}
