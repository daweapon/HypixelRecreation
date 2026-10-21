package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

import dev.daweapon.type.skyblockgeneric.hunting.creature.HuntingCreatureMob;
import dev.daweapon.type.skyblockgeneric.mythological.ability.MobProjectile;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.MobAttackService;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

final class ProjectileSpecial implements CreatureSpecial {
    private static final double ARROW_SPEED = 1.4;
    private static final double AXE_SPEED = 1.0;
    private static final double EYE = 1.2;

    @FunctionalInterface
    interface Shot {
        void fire(HuntingCreatureMob mob, SkyBlockPlayer target);
    }

    private final double range;
    private final long intervalTicks;
    private final boolean needsSight;
    private final Shot shot;

    private ProjectileSpecial(double range, long intervalTicks, boolean needsSight, Shot shot) {
        this.range = range;
        this.intervalTicks = intervalTicks;
        this.needsSight = needsSight;
        this.shot = shot;
    }

    static ProjectileSpecial bow(double range, long intervalTicks) {
        return new ProjectileSpecial(range, intervalTicks, true, (mob, target) ->
                MobProjectile.arrow(mob, target, ARROW_SPEED, hit -> MobAttackService.strike(mob, hit, mob.spec().damage())));
    }

    static ProjectileSpecial axe(double range, long intervalTicks) {
        ItemStack axe = ItemStack.of(Material.IRON_AXE);
        return new ProjectileSpecial(range, intervalTicks, false, (mob, target) ->
                MobProjectile.item(mob, target, axe, AXE_SPEED, hit -> MobAttackService.strike(mob, hit, mob.spec().damage())));
    }

    @Override
    public void onTick(HuntingCreatureMob mob, long ticks) {
        if (ticks % intervalTicks != 0 || mob.getInstance() == null) return;
        CreatureMotion.nearestPlayer(mob, range)
                .filter(player -> !needsSight || CreatureMotion.lineOfSight(mob.getInstance(),
                        mob.getPosition().add(0, EYE, 0), player.getPosition().add(0, 1, 0)))
                .ifPresent(player -> shot.fire(mob, player));
    }
}
