package net.swofty.type.skyblockgeneric.slayer;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class SlayerDropParser {
    private SlayerDropParser() {
    }

    static List<SlayerDrop> drops(List<Map<String, Object>> entries) {
        List<SlayerDrop> result = new ArrayList<>();
        for (Map<String, Object> entry : entries) {
            result.add(new SlayerDrop(
                item(entry.get("item")),
                (String) entry.get("name"),
                entry.get("enchant") == null ? null : EnchantmentType.valueOf(String.valueOf(entry.get("enchant"))),
                number(entry.get("enchantLevel"), 1).intValue(),
                SlayerDrop.Pool.valueOf(String.valueOf(entry.get("pool"))),
                number(entry.get("level"), 0).intValue(),
                number(entry.get("rng"), 0).longValue(),
                (String) entry.get("unavailable"),
                odds((Map<String, Object>) entry.get("tiers"))
            ));
        }
        return List.copyOf(result);
    }

    static List<SlayerMiniboss> minibosses(List<Map<String, Object>> entries) {
        List<SlayerMiniboss> result = new ArrayList<>();
        for (Map<String, Object> entry : entries) {
            Set<SlayerTier> tiers = EnumSet.noneOf(SlayerTier.class);
            for (Object tier : (List<Object>) entry.getOrDefault("tiers", List.of())) {
                tiers.add(SlayerTier.valueOf(String.valueOf(tier)));
            }
            result.add(new SlayerMiniboss(
                String.valueOf(entry.get("id")),
                String.valueOf(entry.get("name")),
                tiers,
                Boolean.TRUE.equals(entry.get("stronger")),
                number(entry.get("level"), 1).intValue(),
                number(entry.get("health"), 100).doubleValue(),
                number(entry.get("damage"), 1).doubleValue(),
                number(entry.get("combatXp"), 0).intValue(),
                item(entry.get("token")),
                number(entry.get("tokenAmount"), 0).intValue()
            ));
        }
        return List.copyOf(result);
    }

    private static Map<SlayerTier, SlayerDrop.TierOdds> odds(@Nullable Map<String, Object> entry) {
        Map<SlayerTier, SlayerDrop.TierOdds> result = new EnumMap<>(SlayerTier.class);
        if (entry == null) return result;
        entry.forEach((tier, value) -> {
            List<Number> numbers = (List<Number>) value;
            result.put(SlayerTier.valueOf(tier), new SlayerDrop.TierOdds(
                numbers.get(0).intValue(), numbers.get(1).intValue(), numbers.get(2).doubleValue()));
        });
        return result;
    }

    private static @Nullable ItemType item(@Nullable Object value) {
        return value == null ? null : ItemType.get(String.valueOf(value));
    }

    private static Number number(@Nullable Object value, Number fallback) {
        if (value instanceof Number number) return number;
        return value == null ? fallback : Double.parseDouble(String.valueOf(value));
    }
}
