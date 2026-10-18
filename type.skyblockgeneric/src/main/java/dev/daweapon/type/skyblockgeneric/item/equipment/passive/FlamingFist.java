package dev.daweapon.type.skyblockgeneric.item.equipment.passive;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.DamageIndicator;

public final class FlamingFist {
    private FlamingFist() {
    }

    public static void fire(SkyBlockPlayer player, SkyBlockMob target) {
        for (int fist = 0; fist < EquipmentPassiveMath.FLAMING_FIST_COUNT; fist++) {
            MinecraftServer.getSchedulerManager().scheduleTask(() -> hit(player, target),
                    TaskSchedule.tick(1 + fist * EquipmentPassiveMath.FLAMING_FIST_SPACING_TICKS), TaskSchedule.stop());
        }
    }

    private static void hit(SkyBlockPlayer player, SkyBlockMob target) {
        if (target.isDead() || target.isRemoved() || target.getInstance() == null) return;
        float damage = (float) EquipmentPassiveMath.FLAMING_FIST_DAMAGE;
        target.damage(new Damage(DamageType.ON_FIRE, player, player, player.getPosition(), damage));
        new DamageIndicator().damage(damage).pos(target.getPosition()).critical(false).display(target.getInstance());
    }
}
