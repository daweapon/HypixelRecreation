package net.swofty.type.skyblockgeneric.fishing.lava;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.crimson.volcano.VolcanoEruptionService;
import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;
import net.swofty.type.skyblockgeneric.hunting.AttributeEffectService;
import net.swofty.type.skyblockgeneric.hunting.AttributeId;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;
import net.swofty.type.skyblockgeneric.loottable.SkyBlockLootTable;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Corrupted Sea Creatures (wiki: Corrupted_Mobs): 1% of Crimson Isle mobs spawn Corrupted (5% while the volcano
 * erupts), plus 1% per Corruption level on the rod and the Magma Slug Shard's Infection attribute. They drop a
 * Corrupted Fragment and Sulphur, and can drop Obfuscated-1. Lord Jawbus is never Corrupted.
 */
public final class CorruptedSeaCreatures {
    static final double BASE_CHANCE = 1D;
    static final double ERUPTION_CHANCE = 5D;
    static final double OBFUSCATED_DROP_CHANCE = 20D;

    private CorruptedSeaCreatures() {
    }

    public static double chance(SkyBlockPlayer angler, SkyBlockItem rod) {
        double chance = VolcanoEruptionService.isErupting() ? ERUPTION_CHANCE : BASE_CHANCE;
        var corruption = rod.getAttributeHandler().getEnchantment(EnchantmentType.CORRUPTION);
        if (corruption != null) chance += corruption.level();
        chance += AttributeEffectService.value(angler.getHuntingData(), AttributeId.parse("U32"));
        return chance;
    }

    public static boolean roll(SkyBlockPlayer angler, SkyBlockItem rod) {
        return LootRoll.roll(angler.getUuid(), null, chance(angler, rod));
    }

    static List<SkyBlockLootTable.LootRecord> extraDrops() {
        List<SkyBlockLootTable.LootRecord> records = new ArrayList<>();
        records.add(new SkyBlockLootTable.LootRecord(ItemType.CORRUPTED_FRAGMENT, 1, 100));
        records.add(new SkyBlockLootTable.LootRecord(ItemType.SULPHUR, 1, 100));
        records.add(new SkyBlockLootTable.LootRecord(ItemType.OBFUSCATED_FISH_1_BRONZE, 1, OBFUSCATED_DROP_CHANCE));
        return records;
    }
}
