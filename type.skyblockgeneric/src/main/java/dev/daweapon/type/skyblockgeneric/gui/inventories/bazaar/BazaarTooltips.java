package dev.daweapon.type.skyblockgeneric.gui.inventories.bazaar;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.bazaar.BazaarItemSet;
import dev.daweapon.type.skyblockgeneric.bazaar.BazaarProduct;
import dev.daweapon.type.skyblockgeneric.bazaar.BazaarProductGroup;
import dev.daweapon.type.skyblockgeneric.bazaar.BazaarProducts;
import dev.daweapon.type.skyblockgeneric.bazaar.BazaarTile;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class BazaarTooltips {
    private static final DecimalFormatSymbols SYMBOLS = DecimalFormatSymbols.getInstance(Locale.US);
    private static final DecimalFormat WHOLE = new DecimalFormat("#,##0", SYMBOLS);
    private static final DecimalFormat TENTH = new DecimalFormat("#,##0.0", SYMBOLS);
    private static final double TENTH_BELOW = 10_000;

    private BazaarTooltips() {
    }

    static String coins(double price) {
        return price < TENTH_BELOW ? TENTH.format(price) : WHOLE.format(price);
    }

    static Text productCount(int count) {
        return count == 1 ? Text.key("gui_bazaar.tile.product_count_one") : Text.key("gui_bazaar.tile.product_count", count);
    }

    static Text arrow(Rarity rarity, String name) {
        return Text.of("<color:{}>▶ <white>{}", rarity.getColor(), name);
    }

    static List<Text> groupLore(BazaarTile tile) {
        List<Text> lore = new ArrayList<>();
        lore.add(productCount(tile.productCount()));
        lore.add(Text.literal(" "));
        String description = tile.descriptionKey();
        if (description != null) {
            lore.addAll(Text.keyLines(description));
        } else if (tile.nested()) {
            for (BazaarItemSet set : tile.sets()) {
                lore.add(arrow(set.arrowRarity(), set.name()));
            }
        } else {
            for (BazaarProduct product : tile.first().products()) {
                lore.add(arrow(product.rarity(), product.name()));
            }
        }
        lore.add(Text.literal(" "));
        lore.add(Text.key("gui_bazaar.tile.click_to_view_products"));
        return lore;
    }

    static List<Text> nestedEntryLore(BazaarItemSet set) {
        return groupLore(new BazaarTile(set.name(), List.of(set), false, set.gridPosition));
    }

    static List<Text> priceLore(Double buy, Double sell) {
        List<Text> lore = new ArrayList<>();
        lore.add(productCount(1));
        lore.add(Text.literal(" "));
        lore.addAll(prices(buy, sell));
        lore.add(Text.literal(" "));
        lore.add(Text.key("gui_bazaar.tile.click_to_view_product"));
        return lore;
    }

    static List<Text> prices(Double buy, Double sell) {
        List<Text> lines = new ArrayList<>();
        lines.add(buy == null || buy <= 0 ? Text.key("gui_bazaar.tile.buy_price_none") : Text.key("gui_bazaar.tile.buy_price", coins(buy)));
        lines.add(sell == null || sell <= 0 ? Text.key("gui_bazaar.tile.sell_price_none") : Text.key("gui_bazaar.tile.sell_price", coins(sell)));
        return lines;
    }

    static List<Text> groupLore(BazaarProductGroup group) {
        List<Text> lore = new ArrayList<>();
        lore.add(productCount(group.products().size()));
        lore.add(Text.literal(" "));
        for (BazaarProduct product : group.products()) {
            lore.add(arrow(product.rarity(), product.name()));
        }
        lore.add(Text.literal(" "));
        lore.add(Text.key("gui_bazaar.tile.click_to_view_products"));
        return lore;
    }

    static List<Text> productLore(BazaarProduct product, Double buy, Double sell) {
        List<Text> lore = new ArrayList<>();
        lore.add(Text.key("gui_bazaar.tile.commodity", StringUtility.toNormalCase(product.rarity().name())));
        lore.add(Text.literal(" "));
        lore.addAll(prices(buy, sell));
        lore.add(Text.literal(" "));
        lore.add(Text.key("gui_bazaar.tile.click_to_view_details"));
        return lore;
    }

    static Text productName(ItemType type) {
        return productName(BazaarProducts.of(type));
    }

    static Text productName(BazaarProduct product) {
        return Text.of("<color:{}>{}", product.rarity().getColor(), product.name());
    }
}
