package dev.daweapon.type.skyblockgeneric.shop.catalogue;

import net.minestom.server.item.Material;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.attribute.attributes.ItemAttributeMinionData;
import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;
import dev.daweapon.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
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
        @Nullable String unavailableReason,
        @Nullable ReputationRequirement requirement,
        @Nullable String enchantment,
        int enchantmentLevel,
        int minionTier
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
                raw.get("unavailable") == null ? null : String.valueOf(raw.get("unavailable")),
                ReputationRequirement.parse(raw.get("requires")),
                raw.get("enchantment") == null ? null : String.valueOf(raw.get("enchantment")),
                raw.get("enchantment_level") instanceof Number level ? level.intValue() : 1,
                raw.get("tier") instanceof Number tier ? tier.intValue() : 0
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

    public SkyBlockItem buildItem() {
        SkyBlockItem item = new SkyBlockItem(itemType());
        if (enchantment != null) {
            item.getAttributeHandler().addEnchantment(new SkyBlockEnchantment(EnchantmentType.valueOf(enchantment), enchantmentLevel));
        }
        if (minionTier > 0) {
            item.getAttributeHandler().setMinionData(new ItemAttributeMinionData.MinionData(minionTier, 0));
        }
        return item;
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
