package net.swofty.type.skyblockgeneric.accessory.power;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointAccessoryPowers.PowerData;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** Power Stones: their tooltip and learning a power by giving Maxwell nine identical stones. */
public final class PowerStones {
    public static final int LORE_ACCESSORY_POWER = 250;

    private PowerStones() {
    }

    public static List<String> lore(SkyBlockItem item) {
        AccessoryPowerType power = AccessoryPowerType.byStone(item.getAttributeHandler().getPotentialType());
        if (power == null) return List.of();
        List<String> lore = new ArrayList<>();
        lore.add("<7>Combine <a>x" + AccessoryPowerType.STONES_TO_LEARN + " <7>of this item at the");
        lore.add("<6>Thaumaturgist <7>in the <b>Hub <7>to unlock the");
        lore.add("<a>" + power.getDisplayName() + " <7>power.");
        lore.add("");
        lore.add("<7>At <6>" + LORE_ACCESSORY_POWER + " Accessory Power<7>:");
        lore.addAll(StatText.lines(PowerScaling.scaled(power, LORE_ACCESSORY_POWER)));
        if (!power.getUniqueBonus().isEmpty()) {
            lore.add("");
            lore.add("<7>Unique Power Bonus:");
            lore.addAll(StatText.lines(power.getUniqueBonus()));
        }
        lore.add("");
        lore.add("<4>❣ <c>Requires <a>Combat Skill " + power.getCombatLevel() + "<c>.");
        if (!power.isSourceActive()) {
            lore.add("");
            lore.add("<8>Source: Not active yet");
        }
        return lore;
    }

    public enum LearnResult {
        LEARNED, NOT_A_STONE, MIXED, MISSING, ALREADY_KNOWN, COMBAT_TOO_LOW
    }

    /**
     * Checks nine stacks from Maxwell's combining grid. They must all be the same Power Stone and add
     * up to at least nine; on success the power is learned and nine stones should be removed.
     */
    public static LearnResult check(PowerData data, List<SkyBlockItem> grid, int combatLevel) {
        ItemType stone = null;
        int count = 0;
        for (SkyBlockItem item : grid) {
            if (item == null || item.isNA() || item.isAir()) continue;
            ItemType type = item.getAttributeHandler().getPotentialType();
            if (AccessoryPowerType.byStone(type) == null) return LearnResult.NOT_A_STONE;
            if (stone != null && stone != type) return LearnResult.MIXED;
            stone = type;
            count += item.getAmount();
        }
        if (stone == null || count < AccessoryPowerType.STONES_TO_LEARN) return LearnResult.MISSING;
        AccessoryPowerType power = AccessoryPowerType.byStone(stone);
        if (data.getLearned().contains(power)) return LearnResult.ALREADY_KNOWN;
        if (!AccessoryPowers.meetsRequirement(power, combatLevel)) return LearnResult.COMBAT_TOO_LOW;
        return LearnResult.LEARNED;
    }

    public static @Nullable AccessoryPowerType stonePower(List<SkyBlockItem> grid) {
        for (SkyBlockItem item : grid) {
            if (item == null || item.isNA() || item.isAir()) continue;
            return AccessoryPowerType.byStone(item.getAttributeHandler().getPotentialType());
        }
        return null;
    }

    public static void learnFrom(SkyBlockPlayer player, PowerData data, AccessoryPowerType power) {
        if (!AccessoryPowers.learn(data, power)) return;
        AccessoryPowers.save(player, data);
        AccessoryPowers.awardLearnXP(player, 1);
    }
}
