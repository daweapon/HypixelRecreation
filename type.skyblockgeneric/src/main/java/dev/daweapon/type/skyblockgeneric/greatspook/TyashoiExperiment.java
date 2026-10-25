package dev.daweapon.type.skyblockgeneric.greatspook;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.Rarity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public record TyashoiExperiment(String id, Kind kind, @Nullable String resultItem, @Nullable String enchantment,
                                int enchantmentLevel, @Nullable Rarity eerieUpgrade, List<Cost> costs,
                                @Nullable String requires, long lockoutSeconds, List<String> hint,
                                List<String> info, List<String> success) {

    public enum Kind {ITEM, PET, BOOK}

    public record Cost(@Nullable String item, @Nullable String name, int amount, long coins) {
        public @Nullable ItemType type() {
            return item == null ? null : ItemType.get(item);
        }

        public boolean isCoins() {
            return coins > 0;
        }

        public String label() {
            ItemType type = type();
            if (type != null) return type.getDisplayName();
            return name == null ? item : name;
        }
    }

    @SuppressWarnings("unchecked")
    static TyashoiExperiment parse(Map<String, Object> raw) {
        Map<String, Object> result = (Map<String, Object>) raw.get("result");
        List<Map<String, Object>> rawCosts = (List<Map<String, Object>>) raw.get("costs");
        List<Cost> costs = rawCosts.stream().map(cost -> new Cost(
                (String) cost.get("item"), (String) cost.get("name"),
                ((Number) cost.getOrDefault("amount", 0)).intValue(),
                ((Number) cost.getOrDefault("coins", 0)).longValue())).toList();
        Object upgrade = result.get("eerie_upgrade");
        return new TyashoiExperiment((String) raw.get("id"), Kind.valueOf((String) result.get("kind")),
                (String) result.get("item"), (String) result.get("enchantment"),
                ((Number) result.getOrDefault("level", 1)).intValue(),
                upgrade == null ? null : Rarity.getRarity(upgrade.toString()), costs,
                (String) raw.get("requires"), ((Number) raw.getOrDefault("lockout_seconds", 0)).longValue(),
                (List<String>) raw.get("hint"), (List<String>) raw.getOrDefault("info", List.of()),
                (List<String>) raw.get("success"));
    }

    public String name() {
        if (kind == Kind.BOOK) return enchantment.equals("GREAT_SPOOK") ? "Great Spook I" : enchantment;
        ItemType type = ItemType.get(resultItem);
        return type == null ? resultItem : type.getDisplayName();
    }
}
