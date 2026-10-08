package net.swofty.type.skyblockgeneric.fishing.lava;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.skyblockgeneric.fishing.FishingContext;
import net.swofty.type.skyblockgeneric.fishing.FishingMedium;
import net.swofty.type.skyblockgeneric.fishing.hotspot.FishingHotspotService;
import net.swofty.type.skyblockgeneric.fishing.registry.FishingTableDefinition.SeaCreatureRoll;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;

import java.util.List;
import java.util.Optional;

/**
 * Pools that replace a regular lava Sea Creature catch (wiki: Plhlegblast, Fishing_Hotspots). In the Plhlegblast
 * Pool a catch becomes a Plhlegblast 0.1% of the time (Fishing 36+). In a lava hotspot it comes from the hotspot's
 * pool 15% of the time, +20% with Hotspot Bait, capped at 85%.
 */
public final class LavaCatchOverrides {
    public static final Pos PLHLEGBLAST_POOL = new Pos(-382, 81, -703);
    static final double PLHLEGBLAST_POOL_RADIUS = 8;
    static final double PLHLEGBLAST_CHANCE = 0.1;
    static final int PLHLEGBLAST_FISHING_LEVEL = 36;
    static final double HOTSPOT_BASE_CHANCE = 15;
    static final double HOTSPOT_BAIT_BONUS = 20;
    static final double HOTSPOT_CAP = 85;

    private LavaCatchOverrides() {
    }

    public static Optional<List<SeaCreatureRoll>> override(FishingContext context) {
        if (context.medium() != FishingMedium.LAVA) return Optional.empty();
        if (inPlhlegblastPool(context)
                && context.player().getSkills().getCurrentLevel(SkillCategories.FISHING) >= PLHLEGBLAST_FISHING_LEVEL
                && LootRoll.roll(context.player().getUuid(), null, PLHLEGBLAST_CHANCE)) {
            return Optional.of(List.of(new SeaCreatureRoll(LavaSeaCreature.PLHLEGBLAST.name(), 0, 1)));
        }
        return FishingHotspotService.activeAt(context.player(), context.medium(), context.hookPosition())
                .filter(hotspot -> !hotspot.definition().seaCreatures().isEmpty())
                .filter(_ -> LootRoll.roll(context.player().getUuid(), null, hotspotChance(context)))
                .map(hotspot -> hotspot.definition().seaCreatures());
    }

    static double hotspotChance(FishingContext context) {
        double chance = HOTSPOT_BASE_CHANCE;
        if (context.bait() != null && "HOTSPOT_BAIT".equals(context.bait().getItemId())) chance += HOTSPOT_BAIT_BONUS;
        return Math.min(HOTSPOT_CAP, chance);
    }

    private static boolean inPlhlegblastPool(FishingContext context) {
        if ("PLHLEGBLAST_POOL".equals(context.regionId())) return true;
        Pos hook = context.hookPosition();
        double dx = hook.x() - PLHLEGBLAST_POOL.x();
        double dz = hook.z() - PLHLEGBLAST_POOL.z();
        return dx * dx + dz * dz <= PLHLEGBLAST_POOL_RADIUS * PLHLEGBLAST_POOL_RADIUS;
    }
}
