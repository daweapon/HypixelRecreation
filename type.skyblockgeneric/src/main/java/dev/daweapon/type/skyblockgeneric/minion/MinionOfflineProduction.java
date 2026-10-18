package dev.daweapon.type.skyblockgeneric.minion;

import dev.daweapon.type.skyblockgeneric.command.groups.world.MinionGenerationCommand;
import dev.daweapon.type.skyblockgeneric.item.ItemQuantifiable;
import dev.daweapon.type.skyblockgeneric.minion.storage.MinionStorage;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.minion.extension.extensions.MinionShippingExtension;
import net.minestom.server.instance.Instance;

import java.time.Duration;
import java.util.List;
import java.util.function.IntSupplier;

public final class MinionOfflineProduction {
    public static final long MAX_CATCH_UP_MILLIS = Duration.ofDays(30).toMillis();

    private MinionOfflineProduction() {
    }

    public static double actionIntervalMillis(double timeBetweenActionsSeconds, int divisionFactor, double speedDivisor) {
        return timeBetweenActionsSeconds * 1000.0 / divisionFactor / speedDivisor;
    }

    public static long maxCycles(long elapsedMillis, double actionIntervalMillis, int actionsPerCycle) {
        if (elapsedMillis <= 0 || actionIntervalMillis <= 0 || actionsPerCycle <= 0) return 0;
        long capped = Math.min(elapsedMillis, MAX_CATCH_UP_MILLIS);
        return (long) Math.floor(capped / actionIntervalMillis / actionsPerCycle);
    }

    public static long simulate(long elapsedMillis, double actionIntervalMillis, int actionsPerCycle,
                                IntSupplier freeCapacity, Runnable cycle) {
        long cycles = maxCycles(elapsedMillis, actionIntervalMillis, actionsPerCycle);
        long completed = 0;
        while (completed < cycles && freeCapacity.getAsInt() > 0) {
            cycle.run();
            completed++;
        }
        return completed;
    }

    public static long credit(IslandMinionData.IslandMinion minion, Instance instance, long nowMillis) {
        long since = minion.getOfflineSince().getAndSet(0);
        if (since <= 0) return 0;

        SkyBlockMinion definition = minion.getMinion().asSkyBlockMinion();
        MinionAction action = definition.getAction();
        SkyBlockMinion.MinionTier tier = definition.getTiers().get(minion.getTier() - 1);
        double interval = actionIntervalMillis(tier.timeBetweenActions(), MinionGenerationCommand.divisionFactor,
                minion.getSpeedDivisor(instance));

        long completed = simulate(nowMillis - since, interval, action.actionsPerItem(),
                () -> freeCapacity(minion, tier),
                () -> {
                    List<SkyBlockItem> items = action.offlineYield(minion);
                    if (!items.isEmpty()) MinionAction.onMinionIteration(minion, definition, items);
                });
        MinionOfflineHooks.credit(minion, nowMillis - since);
        minion.setLastAction(nowMillis);
        return completed;
    }

    private static int freeCapacity(IslandMinionData.IslandMinion minion, SkyBlockMinion.MinionTier tier) {
        MinionShippingExtension shipping = (MinionShippingExtension)
                minion.getExtensionData().getOfType(MinionShippingExtension.class);
        if (shipping.getItemTypePassedIn() != null) return Integer.MAX_VALUE;
        int stored = minion.getItemsInMinion().stream().mapToInt(ItemQuantifiable::getAmount).sum();
        return tier.getSlots() * 64 - stored + MinionStorage.extraCapacity(minion) - MinionStorage.extraStored(minion);
    }
}
