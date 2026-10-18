package dev.daweapon.type.skyblockgeneric.minion;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class MinionBonusDrops {
    public static final double SOULFLOW_OUTPUT_FACTOR = 0.5;
    public static final double POTATO_SPREADING_CHANCE_PERCENT = 5;
    private static final int MAX_SHEARED_WOOL = 3;

    private MinionBonusDrops() {
    }

    public static boolean hasSoulflowEngine(IslandMinionData.IslandMinion minion) {
        return minion.getExtensionData().hasMinionUpgrade(ItemType.LESSER_SOULFLOW_ENGINE)
                || minion.getExtensionData().hasMinionUpgrade(ItemType.SOULFLOW_ENGINE);
    }

    public static SkyBlockItem convert(SkyBlockItem generated, IslandMinionData.IslandMinion minion) {
        if (minion.getMinion() != MinionRegistry.POTATO || !minion.getExtensionData().hasMinionUpgrade(ItemType.HUNTER_KNIFE)) {
            return generated;
        }
        if (generated.getAttributeHandler().getPotentialType() != ItemType.POTATO) return generated;
        return new SkyBlockItem(ItemType.FRENCH_FRIES, generated.getAmount());
    }

    public static SkyBlockItem applyOutputPenalty(SkyBlockItem item, IslandMinionData.IslandMinion minion) {
        if (!hasSoulflowEngine(minion)) return item;
        item.setAmount(scaledAmount(item.getAmount(), SOULFLOW_OUTPUT_FACTOR, item.getAttributeHandler().getPotentialType()));
        return item;
    }

    public static List<SkyBlockItem> bonusDrops(IslandMinionData.IslandMinion minion, List<SkyBlockItem> produced) {
        List<SkyBlockItem> bonus = new ArrayList<>();
        int baseDrops = produced.stream().mapToInt(SkyBlockItem::getAmount).sum();
        var upgrades = minion.getExtensionData();

        if (minion.getMinion() == MinionRegistry.CHICKEN && upgrades.hasMinionUpgrade(ItemType.ENCHANTED_EGG)) {
            bonus.add(new SkyBlockItem(ItemType.EGG, 1));
        }
        if (minion.getMinion() == MinionRegistry.SHEEP && upgrades.hasMinionUpgrade(ItemType.ENCHANTED_SHEARS)) {
            int wool = ThreadLocalRandom.current().nextInt(MAX_SHEARED_WOOL);
            if (wool > 0) bonus.add(new SkyBlockItem(ItemType.WHITE_WOOL, wool));
        }
        if (upgrades.hasMinionUpgrade(ItemType.POTATO_SPREADING)) {
            int potatoes = 0;
            for (int i = 0; i < baseDrops; i++) {
                if (LootRoll.roll(null, ItemType.POTATO, POTATO_SPREADING_CHANCE_PERCENT)) potatoes++;
            }
            if (potatoes > 0) bonus.add(new SkyBlockItem(ItemType.POTATO, potatoes));
        }
        return bonus;
    }

    public static int scaledAmount(int amount, double factor, ItemType rollItem) {
        double scaled = amount * factor;
        int whole = (int) Math.floor(scaled);
        double fraction = scaled - whole;
        if (fraction > 0 && LootRoll.roll(null, rollItem, fraction * 100)) whole++;
        return whole;
    }
}
