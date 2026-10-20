package dev.daweapon.type.skyblockgeneric.gui.inventories.starlyn;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynConfig;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynCost;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynSister;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class StarlynGui {
    static final int BAR_LENGTH = 25;
    static final int[] GRID = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};

    private StarlynGui() {
    }

    static String bar(long value, long total) {
        int filled = total <= 0 ? BAR_LENGTH : (int) Math.min(BAR_LENGTH, value * BAR_LENGTH / total);
        return "<a>" + "—".repeat(filled) + "<f>" + "—".repeat(BAR_LENGTH - filled);
    }

    static String compact(int value) {
        if (value >= 1000 && value % 1000 == 0) return (value / 1000) + "k";
        if (value >= 1000) return String.format(Locale.ROOT, "%.1fk", value / 1000D);
        return Integer.toString(value);
    }

    static String name(ItemType type) {
        return new SkyBlockItem(type).getDisplayName();
    }

    static List<String> costLines(StarlynSister sister, StarlynCost cost, boolean itemsFirst) {
        List<String> lines = new ArrayList<>();
        lines.add("<7>Cost");
        if (itemsFirst) itemLines(cost, lines);
        if (cost.coupons() > 0) lines.add("<a>" + sister.couponName() + " <8>x" + StringUtility.commaify(cost.coupons()));
        if (cost.prizes() > 0) lines.add("<9>" + sister.prizeName() + " <8>x" + cost.prizes());
        if (!itemsFirst) itemLines(cost, lines);
        return lines;
    }

    private static void itemLines(StarlynCost cost, List<String> lines) {
        for (Map.Entry<ItemType, Integer> item : cost.items().entrySet()) {
            lines.add(name(item.getKey()) + (item.getValue() > 1 ? " <8>x" + StringUtility.commaify(item.getValue()) : ""));
        }
    }

    static List<String> rewardLines(StarlynSister sister, StarlynConfig.BracketReward reward) {
        List<String> lines = new ArrayList<>();
        lines.add("<7>Rewards:");
        lines.add("<a>  " + sister.couponName() + " <8>x" + reward.coupons());
        if (reward.prizeChance() > 0) {
            lines.add("<9>   " + sister.prizeName() + " <7>(<a>" + (int) reward.prizeChance() + "% <7>chance)");
        }
        lines.add("<8>  +<f>" + reward.essence() + "x <d>Forest Essence");
        lines.add("<8>  +<3>" + StringUtility.commaify(reward.foragingXp()) + " <7>Foraging Experience");
        lines.add("<8>  +<a>" + reward.hotfXp() + " HOTF Exp");
        lines.add("<8>  +" + sister.getCurrency().getColour() + StringUtility.commaify(reward.whispers()) + " <7>" + sister.getCurrency().getDisplayName());
        if (reward.safariTicket() != null) lines.add("<8>  +" + ticketName(reward.safariTicket()));
        lines.add("<8>  +<b>" + reward.bits() + " Bits");
        return lines;
    }

    static String ticketName(String id) {
        ItemType type = ItemType.get(id);
        if (type != null) return name(type);
        StringBuilder title = new StringBuilder("<8>");
        for (String word : id.toLowerCase(Locale.ROOT).split("_")) {
            title.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(' ');
        }
        return title.toString().trim() + " (not in game yet)";
    }
}
