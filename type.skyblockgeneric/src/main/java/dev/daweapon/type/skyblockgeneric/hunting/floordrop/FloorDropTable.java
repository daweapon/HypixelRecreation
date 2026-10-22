package dev.daweapon.type.skyblockgeneric.hunting.floordrop;

import java.util.List;
import java.util.Map;

public record FloorDropTable(int maxActive, int spawnSeconds, double minDistance, double maxDistance, int lifetimeSeconds,
                             List<Reward> rewards) {
    public enum Kind { SHARD, WHISPERS, ITEM }

    public record Reward(Kind kind, String id, int amount, double weight) {
    }

    @SuppressWarnings("unchecked")
    public static FloorDropTable parse(Map<String, Object> raw) {
        List<Reward> rewards = ((List<Map<String, Object>>) raw.get("rewards")).stream().map(entry -> {
            Kind kind = Kind.valueOf(String.valueOf(entry.get("type")));
            String id = String.valueOf(entry.getOrDefault(kind == Kind.SHARD ? "shard" : "item", ""));
            return new Reward(kind, id, number(entry.get("amount"), 1).intValue(), number(entry.get("weight"), 1).doubleValue());
        }).toList();
        return new FloorDropTable(number(raw.get("maxActive"), 5).intValue(), number(raw.get("spawnSeconds"), 12).intValue(),
                number(raw.get("minDistance"), 10).doubleValue(), number(raw.get("maxDistance"), 35).doubleValue(),
                number(raw.get("lifetimeSeconds"), 900).intValue(), rewards);
    }

    public double totalWeight() {
        return rewards.stream().mapToDouble(Reward::weight).sum();
    }

    public double chance(Reward reward) {
        return reward.weight() / totalWeight() * 100;
    }

    private static Number number(Object value, Number fallback) {
        return value instanceof Number number ? number : fallback;
    }
}
