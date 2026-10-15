package dev.daweapon.type.skyblockgeneric.item.set.sets;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.item.armor.ArmorAbilityEffects;
import dev.daweapon.type.skyblockgeneric.item.armor.WornArmor;
import dev.daweapon.type.skyblockgeneric.item.set.impl.SetRepeatable;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class EmberNetherLord implements SetRepeatable {
    public static final int REFRESH_TICKS = 5;
    public static final int EFFECT_TICKS = 40;
    public static final int OBSIDIAN_TICKS = 60;

    @Override
    public Task getTask(Scheduler scheduler) {
        return scheduler.buildTask(() -> {
            for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
                if (player.getInstance() == null || player.isDead()) continue;
                if (!WornArmor.wearsAll(WornArmor.ids(player), ArmorAbilityEffects.EMBER)) continue;
                player.addEffect(new Potion(PotionEffect.FIRE_RESISTANCE, (byte) 0, EFFECT_TICKS));
                walkOnLava(player, scheduler);
            }
        }).repeat(TaskSchedule.tick(REFRESH_TICKS)).schedule();
    }

    private static void walkOnLava(SkyBlockPlayer player, Scheduler scheduler) {
        Instance instance = player.getInstance();
        Pos pos = player.getPosition();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = -1; dy <= 0; dy++) {
                    Pos target = pos.add(dx, dy, dz);
                    if (!instance.getBlock(target).compare(Block.LAVA)) continue;
                    Block lava = instance.getBlock(target);
                    instance.setBlock(target, Block.OBSIDIAN);
                    scheduler.buildTask(() -> {
                        if (instance.getBlock(target).compare(Block.OBSIDIAN)) instance.setBlock(target, lava);
                    }).delay(TaskSchedule.tick(OBSIDIAN_TICKS)).schedule();
                }
            }
        }
    }
}
