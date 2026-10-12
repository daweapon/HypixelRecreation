package net.swofty.type.skyblockgeneric.item.set.sets;

import net.minestom.server.entity.Entity;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.item.armor.WornArmor;
import net.swofty.type.skyblockgeneric.item.set.impl.SetRepeatable;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.AttackService;

public class VampireWitchMaskRejuvenate implements SetRepeatable {
    public static final double DRAIN_PER_SECOND = 5;
    public static final double RANGE = 8;

    @Override
    public Task getTask(Scheduler scheduler) {
        return scheduler.buildTask(() -> {
            for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
                if (player.getInstance() == null || player.isDead()) continue;
                if (!"VAMPIRE_WITCH_MASK".equals(WornArmor.id(player.getHelmet()))) continue;
                for (Entity entity : player.getInstance().getNearbyEntities(player.getPosition(), RANGE)) {
                    if (entity instanceof SkyBlockMob mob && !mob.isDead() && !mob.isRemoved()) {
                        AttackService.applyHit(player, mob, (float) DRAIN_PER_SECOND, false);
                    }
                }
            }
        }).repeat(TaskSchedule.seconds(1)).schedule();
    }
}
