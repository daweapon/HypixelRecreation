package net.swofty.type.skyblockgeneric.shop.catalogue;

import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public record CatalogueEntry(
        String itemId,
        String name,
        Material material,
        int amount,
        CatalogueCost cost,
        boolean stackable,
        List<String> lore,
        @Nullable String unavailableReason
) {
    public static final String MISSING_ITEM_REASON = "<c>This item is not in the game yet!";

    @SuppressWarnings("unchecked")
    public static CatalogueEntry parse(Map<String, Object> raw) {
        String itemId = String.valueOf(raw.get("item"));
        Object material = raw.get("material");
        Object lore = raw.get("lore");
        return new CatalogueEntry(
                itemId,
                String.valueOf(raw.getOrDefault("name", "<f>" + StringUtility.toNormalCase(itemId))),
                material(material == null ? null : String.valueOf(material)),
                raw.get("amount") instanceof Number number ? number.intValue() : 1,
                CatalogueCost.parse(raw.get("cost")),
                Boolean.TRUE.equals(raw.get("stackable")),
                lore instanceof List<?> list ? (List<String>) list : List.of(),
                raw.get("unavailable") == null ? null : String.valueOf(raw.get("unavailable"))
        );
    }

    private static Material material(@Nullable String key) {
        if (key == null) return Material.PAPER;
        Material material = Material.fromKey(key.toLowerCase().contains(":") ? key.toLowerCase() : "minecraft:" + key.toLowerCase());
        return material == null ? Material.PAPER : material;
    }

    public @Nullable ItemType itemType() {
        return ItemType.get(itemId);
    }

    public boolean available() {
        return unavailableReason == null && itemType() != null && cost.purchasable();
    }

    public String reason() {
        if (unavailableReason != null) return unavailableReason;
        if (itemType() == null) return MISSING_ITEM_REASON;
        return "<c>This purchase is not available yet!";
    }
}
