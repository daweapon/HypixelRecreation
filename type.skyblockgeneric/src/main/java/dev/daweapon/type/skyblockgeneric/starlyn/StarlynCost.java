package dev.daweapon.type.skyblockgeneric.starlyn;

import dev.daweapon.commons.skyblock.item.ItemType;

import java.util.LinkedHashMap;
import java.util.Map;

public record StarlynCost(int coupons, int prizes, Map<ItemType, Integer> items) {
    public static final StarlynCost FREE = new StarlynCost(0, 0, Map.of());

    public StarlynCost {
        items = Map.copyOf(items);
    }

    public static StarlynCost coupons(int coupons) {
        return new StarlynCost(coupons, 0, Map.of());
    }

    public StarlynCost plus(StarlynCost other) {
        Map<ItemType, Integer> merged = new LinkedHashMap<>(items);
        other.items.forEach((type, amount) -> merged.merge(type, amount, Integer::sum));
        return new StarlynCost(coupons + other.coupons, prizes + other.prizes, merged);
    }

    public Map<ItemType, Integer> required(StarlynSister sister) {
        Map<ItemType, Integer> all = new LinkedHashMap<>();
        if (coupons > 0) all.put(sister.getCoupon(), coupons);
        if (prizes > 0) all.put(sister.getPrize(), prizes);
        items.forEach((type, amount) -> all.merge(type, amount, Integer::sum));
        return all;
    }

    public int amountOf(ItemType type) {
        return items.getOrDefault(type, 0);
    }
}
