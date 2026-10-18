package dev.daweapon.type.skyblockgeneric.item.set.sets;

import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.item.equipment.passive.EquipmentPassiveMath;
import dev.daweapon.type.skyblockgeneric.item.equipment.passive.EquipmentPassives;
import dev.daweapon.type.skyblockgeneric.item.set.impl.SetRepeatable;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Lava Shell Necklace: while it is worn the player cannot receive healing. Its Health Regen and Mending
 * zeroing is applied in {@link EquipmentPassives#finalise}.
 */
public class LavaShellHealingBlock implements SetRepeatable {
    private static final String SOURCE = "LAVA_SHELL_NECKLACE";
    private static final String BLOCKED = "<c>You cannot receive healing while wearing the Lava Shell Necklace!";

    @Override
    public Task getTask(Scheduler scheduler) {
        return scheduler.buildTask(() -> {
            for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
                if (player.getSkyblockDataHandler() == null) continue;
                if (EquipmentPassives.wearing(player, EquipmentPassiveMath.LAVA_SHELL_NECKLACE)) {
                    player.setHealingModifier(SOURCE, new SkyBlockPlayer.HealingModifier(0, BLOCKED));
                } else {
                    player.removeHealingModifier(SOURCE);
                }
            }
        }).repeat(TaskSchedule.tick(10)).schedule();
    }
}
