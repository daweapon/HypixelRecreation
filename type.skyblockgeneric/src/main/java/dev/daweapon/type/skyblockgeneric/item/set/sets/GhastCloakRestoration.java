package dev.daweapon.type.skyblockgeneric.item.set.sets;

import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointInventory;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.set.impl.SetRepeatable;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Set;

/**
 * Blazing Restoration: the Ghast Cloaks restore health every second while equipped.
 */
public class GhastCloakRestoration implements SetRepeatable {
    private static final Set<ItemType> CLOAKS = Set.of(ItemType.GHAST_CLOAK, ItemType.VANQUISHED_GHAST_CLOAK);
    private static final float HEALTH_PER_SECOND = 5;

    @Override
    public Task getTask(Scheduler scheduler) {
        return scheduler.buildTask(() -> {
            for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
                if (player.getSkyblockDataHandler() == null || !wearsCloak(player)) continue;
                player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + HEALTH_PER_SECOND));
            }
        }).repeat(TaskSchedule.seconds(1)).schedule();
    }

    private static boolean wearsCloak(SkyBlockPlayer player) {
        var cloak = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.INVENTORY, DatapointInventory.class)
                .getValue().getCloak();
        if (cloak == null) return false;
        ItemType type = new SkyBlockItem(cloak).getAttributeHandler().getPotentialType();
        return type != null && CLOAKS.contains(type);
    }
}
