package net.swofty.type.skyblockgeneric.item.set.sets;

import net.minestom.server.potion.PotionEffect;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.item.armor.WornArmor;
import net.swofty.type.skyblockgeneric.item.set.impl.SetRepeatable;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class ObsidianChestplateWard implements SetRepeatable {
    public static final int CHECK_TICKS = 5;

    @Override
    public Task getTask(Scheduler scheduler) {
        return scheduler.buildTask(() -> {
            for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
                if (!"OBSIDIAN_CHESTPLATE".equals(WornArmor.id(player.getChestplate()))) continue;
                if (player.hasEffect(PotionEffect.WITHER)) player.removeEffect(PotionEffect.WITHER);
            }
        }).repeat(TaskSchedule.tick(CHECK_TICKS)).schedule();
    }
}
