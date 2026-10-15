package dev.daweapon.type.skyblockgeneric.garden.greenhouse;

import dev.daweapon.type.skyblockgeneric.garden.GardenProfile;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface MutationBehaviour {
    record Ctx(SkyBlockPlayer player, GardenProfile profile, int plot, GreenhouseData.Bed bed, int anchor,
               GreenhouseData.Plant plant, PlantType type, long now) {
        public int size() {
            return type.footprint();
        }
    }

    enum Harvest {
        PROCEED, BLOCKED, DEFERRED
    }

    default boolean needsWater() {
        return true;
    }

    default boolean ticks(Ctx ctx) {
        return !ctx.plant().grown(ctx.type());
    }

    default boolean beforeStage(Ctx ctx) {
        return true;
    }

    default void onStage(Ctx ctx) {
    }

    default boolean harvestable(Ctx ctx) {
        return ctx.plant().grown(ctx.type());
    }

    default Harvest onHarvest(Ctx ctx, SkyBlockItem held) {
        return Harvest.PROCEED;
    }

    default void afterHarvest(Ctx ctx) {
    }

    default void onDecay(Ctx ctx) {
    }

    default boolean onUse(Ctx ctx, SkyBlockItem held) {
        return false;
    }

    default int itemCount(Ctx ctx) {
        return 1;
    }

    default double dropMultiplier(Ctx ctx) {
        return 1;
    }

    default List<SkyBlockItem> extraDrops(Ctx ctx) {
        return List.of();
    }

    default boolean listensNear() {
        return false;
    }

    default void onNear(Ctx ctx, SkyBlockPlayer nearby) {
    }

    default void onTick(Ctx ctx) {
    }

    default @Nullable String status(Ctx ctx) {
        return null;
    }
}
