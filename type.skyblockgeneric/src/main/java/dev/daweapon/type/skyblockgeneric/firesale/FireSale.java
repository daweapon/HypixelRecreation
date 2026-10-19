package dev.daweapon.type.skyblockgeneric.firesale;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.ConfigurableSkyBlockItem;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public record FireSale(String id, String name, String rarity, int gems, long stock, List<String> items) {
    public static final long UNLIMITED = -1;

    @SuppressWarnings("unchecked")
    static FireSale parse(Map<String, Object> raw) {
        return new FireSale(
                String.valueOf(raw.get("id")),
                String.valueOf(raw.get("name")),
                String.valueOf(raw.get("rarity")),
                ((Number) raw.get("gems")).intValue(),
                ((Number) raw.get("stock")).longValue(),
                (List<String>) raw.get("items"));
    }

    public boolean unlimited() {
        return stock == UNLIMITED;
    }

    public @Nullable String unavailableReason(String itemId) {
        ConfigurableSkyBlockItem config = ConfigurableSkyBlockItem.getFromID(itemId);
        if (config == null || ItemType.get(itemId) == null) return "This item is not in the game yet!";
        return config.getUnavailable();
    }
}
