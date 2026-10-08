package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.gathering;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.AbilityRuntime;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Grants value {0} of each statistic while a block of the given fluid is within a few blocks of
 * the player. The scan is cached for a second.
 */
public final class NearFluidStatHandler implements PetAbilityHandler {
    private static final int RADIUS = 4;
    private static final long RESCAN_MILLIS = 1000;

    private final Block fluid;
    private final ItemStatistic[] statistics;

    public NearFluidStatHandler(Block fluid, ItemStatistic... statistics) {
        this.fluid = fluid;
        this.statistics = statistics;
    }

    @Override
    public ItemStatistics statistics(PetAbilityContext context) {
        SkyBlockPlayer player = context.player();
        AbilityRuntime runtime = context.runtime();
        if (player == null || runtime == null || player.getInstance() == null) return ItemStatistics.empty();
        long now = System.currentTimeMillis();
        if (now - runtime.getLastProc() >= RESCAN_MILLIS) {
            runtime.setPending(isNear(player.getInstance(), player.getPosition()));
            runtime.setLastProc(now);
        }
        if (!runtime.isPending()) return ItemStatistics.empty();
        ItemStatistics.Builder builder = ItemStatistics.builder();
        for (ItemStatistic statistic : statistics) builder.withBase(statistic, context.value(0));
        return builder.build();
    }

    private boolean isNear(Instance instance, Pos position) {
        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int y = -RADIUS; y <= RADIUS; y++) {
                for (int z = -RADIUS; z <= RADIUS; z++) {
                    Pos at = position.add(x, y, z);
                    if (!instance.isChunkLoaded(at)) continue;
                    if (instance.getBlock(at).compare(fluid)) return true;
                }
            }
        }
        return false;
    }
}
