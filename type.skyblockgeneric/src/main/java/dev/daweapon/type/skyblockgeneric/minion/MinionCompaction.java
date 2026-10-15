package dev.daweapon.type.skyblockgeneric.minion;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.ItemQuantifiable;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;

import java.util.ArrayList;
import java.util.List;

public final class MinionCompaction {
    public static final int ENCHANTED_COST = 160;
    private static final String ENCHANTED_PREFIX = "ENCHANTED_";

    private MinionCompaction() {
    }

    public static int enchantedCount(int amount) {
        return Math.max(0, amount) / ENCHANTED_COST;
    }

    public static void compact(IslandMinionData.IslandMinion minion) {
        List<ItemQuantifiable> stored = minion.getItemsInMinion();
        List<ItemQuantifiable> produced = new ArrayList<>();
        for (ItemQuantifiable entry : stored) {
            ItemType type = entry.getItem().getAttributeHandler().getPotentialType();
            if (type == null || type.name().startsWith(ENCHANTED_PREFIX)) continue;
            ItemType enchanted = ItemType.get(ENCHANTED_PREFIX + type.name());
            int crafted = enchantedCount(entry.getAmount());
            if (enchanted == null || crafted == 0) continue;
            entry.setAmount(entry.getAmount() - crafted * ENCHANTED_COST);
            produced.add(new ItemQuantifiable(new SkyBlockItem(enchanted), crafted));
        }
        stored.removeIf(entry -> entry.getAmount() <= 0);
        for (ItemQuantifiable made : produced) {
            ItemQuantifiable existing = stored.stream().filter(e -> e.matchesType(made.getItem())).findFirst().orElse(null);
            if (existing != null) existing.setAmount(existing.getAmount() + made.getAmount());
            else stored.add(made);
        }
    }
}
