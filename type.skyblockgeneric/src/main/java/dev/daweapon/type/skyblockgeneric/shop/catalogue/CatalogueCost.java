package dev.daweapon.type.skyblockgeneric.shop.catalogue;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.essence.EssenceType;
import dev.daweapon.type.skyblockgeneric.shop.ShopPrice;
import dev.daweapon.type.skyblockgeneric.shop.type.BitsShopPrice;
import dev.daweapon.type.skyblockgeneric.shop.type.CoinShopPrice;
import dev.daweapon.type.skyblockgeneric.shop.type.CombinedShopPrice;
import dev.daweapon.type.skyblockgeneric.shop.type.CopperShopPrice;
import dev.daweapon.type.skyblockgeneric.shop.type.EssenceShopPrice;
import dev.daweapon.type.skyblockgeneric.shop.type.GemShopPrice;
import dev.daweapon.type.skyblockgeneric.shop.type.KernelShopPrice;
import dev.daweapon.type.skyblockgeneric.shop.type.PestShopPrice;
import dev.daweapon.type.skyblockgeneric.shop.type.ItemShopPrice;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record CatalogueCost(double coins, int gems, int bits, int copper, int pests, int kernels, Map<String, Integer> items, @Nullable String essenceType, int essence) {

    @SuppressWarnings("unchecked")
    public static CatalogueCost parse(@Nullable Object raw) {
        if (!(raw instanceof Map<?, ?> map)) {
            return new CatalogueCost(0, 0, 0, 0, 0, 0, Map.of(), null, 0);
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
                number(map.get("pests")).intValue(),
                number(map.get("kernels")).intValue(),
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
        if (pests > 0) parts.add(new PestShopPrice(pests));
        if (kernels > 0) parts.add(new KernelShopPrice(kernels));
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
        if (copper > 0) lines.add(Text.of("<c>{} Copper", StringUtility.commaify(copper)));
        if (pests > 0) lines.add(Text.of("<2>{} Pests", StringUtility.commaify(pests)));
        if (kernels > 0) lines.add(Text.of("<e>{} Kernels", StringUtility.commaify(kernels)));
        if (essence > 0) lines.add(Text.of("<d>{} {} Essence", StringUtility.commaify(essence), StringUtility.toNormalCase(essenceType)));
        items.forEach((id, amount) -> lines.add(Text.of("<a>{} <8>x{}", StringUtility.toNormalCase(id), amount)));
        return lines;
    }
}
