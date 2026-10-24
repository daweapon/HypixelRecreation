package dev.daweapon.type.skyblockgeneric.abiphone;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class AbiphoneContactData {
    public record CallStat(int count, long lastCalled) {
    }

    private AbiphoneContactData() {
    }

    public static Map<String, CallStat> callStats(String raw) {
        Map<String, CallStat> stats = new LinkedHashMap<>();
        for (String entry : raw.split(",")) {
            String[] parts = entry.split(":");
            if (parts.length != 3) continue;
            try {
                stats.put(parts[0], new CallStat(Integer.parseInt(parts[1]), Long.parseLong(parts[2])));
            } catch (NumberFormatException ignored) {
            }
        }
        return stats;
    }

    public static String recordCall(String raw, String contactId, long now) {
        Map<String, CallStat> stats = callStats(raw);
        CallStat previous = stats.getOrDefault(contactId, new CallStat(0, 0));
        stats.put(contactId, new CallStat(previous.count() + 1, now));
        List<String> entries = new ArrayList<>();
        stats.forEach((id, stat) -> entries.add(id + ":" + stat.count() + ":" + stat.lastCalled()));
        return String.join(",", entries);
    }

    public static Set<String> doNotDisturb(String raw) {
        Set<String> ids = new LinkedHashSet<>();
        for (String id : raw.split(",")) if (!id.isBlank()) ids.add(id);
        return ids;
    }

    public static String toggleDoNotDisturb(String raw, String contactId) {
        Set<String> ids = doNotDisturb(raw);
        if (!ids.remove(contactId)) ids.add(contactId);
        return String.join(",", ids);
    }
}
