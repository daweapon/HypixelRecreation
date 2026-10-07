package net.swofty.type.skyblockgeneric.shop.catalogue;

import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.shop.ShopPrice;
import net.swofty.type.skyblockgeneric.shop.type.CoinShopPrice;
import net.swofty.type.skyblockgeneric.shop.type.CombinedShopPrice;
import net.swofty.type.skyblockgeneric.shop.type.GemShopPrice;
import net.swofty.type.skyblockgeneric.shop.type.ItemShopPrice;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record CatalogueCost(double coins, int gems, int bits, Map<String, Integer> items, @Nullable String essenceType, int essence) {

    @SuppressWarnings("unchecked")
    public static CatalogueCost parse(@Nullable Object raw) {
        if (!(raw instanceof Map<?, ?> map)) {
            return new CatalogueCost(0, 0, 0, Map.of(), null, 0);
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
                items,
                essence.isEmpty() ? null : String.valueOf(essence.get("type")),
                number(essence.get("amount")).intValue()
        );
    }

    private static Number number(@Nullable Object value) {
        return value instanceof Number number ? number : 0;
    }

    public boolean purchasable() {
        return bits == 0 && essence == 0
                && items.keySet().stream().allMatch(id -> ItemType.get(id) != null);
    }

    public ShopPrice toShopPrice() {
        List<ShopPrice> parts = new ArrayList<>();
        if (coins > 0) parts.add(new CoinShopPrice(coins));
        if (gems > 0) parts.add(new GemShopPrice(gems));
        items.forEach((id, amount) -> parts.add(new ItemShopPrice(ItemType.get(id), amount)));
        if (parts.size() == 1) return parts.getFirst();
        return new CombinedShopPrice(parts);
    }

    public List<Text> display() {
        List<Text> lines = new ArrayList<>();
        if (coins > 0) lines.add(Text.of("<6>{} Coin{}", StringUtility.commaify(coins), coins != 1 ? "s" : ""));
        if (gems > 0) lines.add(Text.of("<a>{} Gems", StringUtility.commaify(gems)));
        if (bits > 0) lines.add(Text.of("<b>{} Bits", StringUtility.commaify(bits)));
        if (essence > 0) lines.add(Text.of("<d>{} {} Essence", StringUtility.commaify(essence), StringUtility.toNormalCase(essenceType)));
        items.forEach((id, amount) -> lines.add(Text.of("<a>{} <8>x{}", StringUtility.toNormalCase(id), amount)));
        return lines;
    }
}
