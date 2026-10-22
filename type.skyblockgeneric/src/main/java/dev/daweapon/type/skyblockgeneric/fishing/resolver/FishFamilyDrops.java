package dev.daweapon.type.skyblockgeneric.fishing.resolver;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.fishing.catches.CatchPayload;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.Optional;

public final class FishFamilyDrops {
    public static final double CHANCE_PERCENT = 0.05;
    public static final long PITY_TREASURES = 2500;

    private static final Map<String, ItemType> BY_TABLE = Map.of(
            "MOONGLADE_MARSH", ItemType.SWAMP_THE_FISH,
            TombFishing.TABLE_ID, ItemType.SWAMP_THE_FISH,
            "TORRHUS_CANYON", ItemType.FISH_THE_FISH);

    private FishFamilyDrops() {
    }

    public static boolean pityDue(long treasureCatches) {
        return treasureCatches >= PITY_TREASURES;
    }

    static ItemType fishFor(String tableId) {
        return BY_TABLE.get(tableId);
    }

    static String counterKey(ItemType fish) {
        return "pity.treasure." + fish.name();
    }

    static Optional<CatchPayload> pity(SkyBlockPlayer player, String tableId, double xp) {
        ItemType fish = fishFor(tableId);
        if (fish == null || !pityDue(player.getHuntingData().counter(counterKey(fish)))) return Optional.empty();
        return Optional.of(award(player, fish, xp));
    }

    static Optional<CatchPayload> chance(SkyBlockPlayer player, String tableId, double xp) {
        ItemType fish = fishFor(tableId);
        if (fish == null || !LootRoll.roll(player.getUuid(), fish, CHANCE_PERCENT)) return Optional.empty();
        return Optional.of(award(player, fish, xp));
    }

    static void countTreasure(SkyBlockPlayer player, String tableId) {
        ItemType fish = fishFor(tableId);
        if (fish != null) player.getHuntingData().incrementCounter(counterKey(fish));
    }

    private static CatchPayload award(SkyBlockPlayer player, ItemType fish, double xp) {
        player.getHuntingData().setCounter(counterKey(fish), 0);
        return new CatchPayload.Special(fish.name(), 1, xp, false);
    }
}
