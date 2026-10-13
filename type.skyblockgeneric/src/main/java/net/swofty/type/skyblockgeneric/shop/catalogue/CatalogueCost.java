package net.swofty.type.skyblockgeneric.shop.catalogue;

import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.essence.EssenceType;
import net.swofty.type.skyblockgeneric.shop.ShopPrice;
import net.swofty.type.skyblockgeneric.shop.type.BitsShopPrice;
import net.swofty.type.skyblockgeneric.shop.type.CoinShopPrice;
import net.swofty.type.skyblockgeneric.shop.type.CombinedShopPrice;
import net.swofty.type.skyblockgeneric.shop.type.CopperShopPrice;
import net.swofty.type.skyblockgeneric.shop.type.EssenceShopPrice;
import net.swofty.type.skyblockgeneric.shop.type.GemShopPrice;
import net.swofty.type.skyblockgeneric.shop.type.ItemShopPrice;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record CatalogueCost(double coins, int gems, int bits, int copper, Map<String, Integer> items, @Nullable String essenceType, int essence) {

    @SuppressWarnings("unchecked")
    public static CatalogueCost parse(@Nullable Object raw) {
        if (!(raw instanceof Map<?, ?> map)) {
            return new CatalogueCost(0, 0, 0, 0, Map.of(), null, 0);
        }
        Map<String, Integer> items = new LinkedHashMap<>();
        Object rawItems = map.get("items");
        if (rawItems instanceof Map<?, ?> itemMap) {
            itemMap.forEach((key, value) -> items.put(String.valueOf(key), ((Number) value).intValue()));
        }
        Map<String, Object> essence = map.get("essence") instanceof Map<?, ?> e ? (Map<String, Object>) e : Map.of();
        return new CatalogueCost(
                number(map.get("coins")).doubleValue(),
                number(map.get("gems")).intValue(),
                number(map.get("bits")).intValue(),
                number(map.get("copper")).intValue(),
                items,
                essence.isEmpty() ? null : String.valueOf(essence.get("type")),
                number(essence.get("amount")).intValue()
        );
    }

    private static Number number(@Nullable Object value) {
        return value instanceof Number number ? number : 0;
    }

    public @Nullable EssenceType essenceKind() {
        if (essenceType == null) return null;
        try {
            return EssenceType.valueOf(essenceType.toUpperCase());
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    public boolean purchasable() {
        return (essence == 0 || essenceKind() != null)
                && items.keySet().stream().allMatch(id -> ItemType.get(id) != null);
    }

    public ShopPrice toShopPrice() {
        return toShopPrice(1);
    }

    /** {@code coinMultiplier} scales only the coin part, the way faction discounts work. */
    public ShopPrice toShopPrice(double coinMultiplier) {
        List<ShopPrice> parts = new ArrayList<>();
        if (coins > 0) parts.add(new CoinShopPrice(coinMultiplier == 1 ? coins : Math.ceil(coins * coinMultiplier)));
        if (gems > 0) parts.add(new GemShopPrice(gems));
        if (bits > 0) parts.add(new BitsShopPrice(bits));
        if (copper > 0) parts.add(new CopperShopPrice(copper));
        if (essence > 0 && essenceKind() != null) parts.add(new EssenceShopPrice(essenceKind(), essence));
        items.forEach((id, amount) -> parts.add(new ItemShopPrice(ItemType.get(id), amount)));
        if (parts.isEmpty()) return new CoinShopPrice(0);
        if (parts.size() == 1) return parts.getFirst();
        return new CombinedShopPrice(parts);
    }

    public List<Text> display() {
        List<Text> lines = new ArrayList<>();
        if (coins > 0) lines.add(Text.of("<6>{} Coin{}", StringUtility.commaify(coins), coins != 1 ? "s" : ""));
        if (gems > 0) lines.add(Text.of("<a>{} Gems", StringUtility.commaify(gems)));
        if (bits > 0) lines.add(Text.of("<b>{} Bits", StringUtility.commaify(bits)));
        if (copper > 0) lines.add(Text.of("<6>{} Copper", StringUtility.commaify(copper)));
        if (essence > 0) lines.add(Text.of("<d>{} {} Essence", StringUtility.commaify(essence), StringUtility.toNormalCase(essenceType)));
        items.forEach((id, amount) -> lines.add(Text.of("<a>{} <8>x{}", StringUtility.toNormalCase(id), amount)));
        return lines;
    }
}
