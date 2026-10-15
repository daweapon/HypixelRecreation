package dev.daweapon.type.skyblockgeneric.item.set.sets;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.CombatPetHooks;
import dev.daweapon.type.skyblockgeneric.item.set.impl.ArmorSet;
import dev.daweapon.type.skyblockgeneric.item.set.impl.SetRepeatable;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class BlazeArmorSet implements ArmorSet, SetRepeatable {
    private static final double RADIUS = 5;
    private static final double MAX_HEALTH_FRACTION = 0.03;
    private static final int BASE_CAP = 500;
    private static final int CAP_PER_STEP = 100;
    private static final int RODS_PER_STEP = 5_000;
    private static final int MAX_CAP = 5_000;

    public static int damageCap(SkyBlockPlayer player) {
        long rods = player.getCollection().get(ItemType.BLAZE_ROD);
        return (int) Math.min(MAX_CAP, BASE_CAP + CAP_PER_STEP * (rods / RODS_PER_STEP));
    }

    @Override
    public String getName() {
        return "Blazing Aura";
    }

    @Override
    public ArrayList<String> getDescription() {
        return new ArrayList<>(List.of(
                "<7>Damages mobs within <a>5<7> blocks for <a>3%",
                "<7>of their max <c>❤ Health <7>per second.",
                "",
                "<7>Max <c>500 <7>damage/s",
                "<8>+100 per 5,000 Blaze Rods collected",
                "<8>(up to 5,000 damage/s)"));
    }

    @Override
    public Task getTask(Scheduler scheduler) {
        return scheduler.buildTask(() -> {
            if (HypixelConst.isIslandServer()) return;
            for (SkyBlockPlayer player : getWearingSet()) burn(player);
        }).repeat(TaskSchedule.seconds(1)).schedule();
    }

    private void burn(SkyBlockPlayer player) {
        if (player.getInstance() == null) return;
        double boost = CombatPetHooks.blazeArmorMultiplier(player);
        double cap = damageCap(player) * boost;
        for (Entity entity : player.getInstance().getNearbyEntities(player.getPosition(), RADIUS)) {
            if (!(entity instanceof SkyBlockMob mob) || mob.isDead()) continue;
            float damage = (float) Math.min(cap, mob.getMaxHealth() * MAX_HEALTH_FRACTION * boost);
            if (damage <= 0) continue;
            mob.damage(new Damage(DamageType.ON_FIRE, player, player, mob.getPosition(), damage));
        }
    }
}
