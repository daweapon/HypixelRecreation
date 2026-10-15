package dev.daweapon.type.skyblockgeneric.minion.inferno;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.loottable.ForcedDrops;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.minion.IslandMinionData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.random.RandomGenerator;

/**
 * What one Inferno Minion harvest yields: a Crude Gabagool, replaced 80% of the time by the fuel's
 * specialty, plus a roll for every unique item while the fuel is Hypergolic-grade.
 */
public final class InfernoMinionProduction {
    public static final double SPECIALTY_CHANCE_PERCENT = 80;

    private InfernoMinionProduction() {
    }

    public record Drop(ItemType item, int amount) {
    }

    public static List<SkyBlockItem> harvest(IslandMinionData.IslandMinion minion, @Nullable Instance island) {
        return roll(activeFuel(minion), minion.getTier(), CapsaicinEyedrops.isActive(minion),
                item -> forcer(island, item), ThreadLocalRandom.current())
                .stream()
                .map(drop -> new SkyBlockItem(drop.item(), drop.amount()))
                .toList();
    }

    public static List<Drop> roll(@Nullable InfernoMinionFuelComponent fuel, int minionTier, boolean eyedrops,
                                  Function<ItemType, @Nullable UUID> forcer, RandomGenerator random) {
        List<Drop> drops = new ArrayList<>();
        if (fuel != null && LootRoll.roll(forcer.apply(fuel.getSpecialty()), fuel.getSpecialty(), SPECIALTY_CHANCE_PERCENT, random)) {
            drops.add(new Drop(fuel.getSpecialty(), random.nextInt(fuel.getMinAmount(), fuel.getMaxAmount() + 1)));
        } else {
            drops.add(new Drop(ItemType.CRUDE_GABAGOOL, 1));
        }

        if (fuel == null || !fuel.getGrade().createsUniqueItems()) return drops;
        for (InfernoRareDrop rare : InfernoRareDrop.values()) {
            if (LootRoll.roll(forcer.apply(rare.getItem()), rare.getItem(), rare.chancePercent(eyedrops), random)) {
                drops.add(new Drop(rare.getItem(), rare.amount(minionTier)));
            }
        }
        return drops;
    }

    public static @Nullable InfernoMinionFuelComponent activeFuel(IslandMinionData.IslandMinion minion) {
        SkyBlockItem fuel = minion.getExtensionData().getFuel();
        if (fuel == null || !fuel.hasComponent(InfernoMinionFuelComponent.class)) return null;
        return fuel.getComponent(InfernoMinionFuelComponent.class);
    }

    /** Lets staff {@code /forcedrop} a minion item while standing on the island. */
    private static @Nullable UUID forcer(@Nullable Instance island, ItemType item) {
        if (island == null) return null;
        for (Player player : island.getPlayers()) {
            if (ForcedDrops.isForced(player.getUuid(), Set.of(item))) return player.getUuid();
        }
        return null;
    }
}
