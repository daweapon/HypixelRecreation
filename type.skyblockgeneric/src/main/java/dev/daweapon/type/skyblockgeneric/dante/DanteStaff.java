package dev.daweapon.type.skyblockgeneric.dante;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.item.handlers.ability.AbilityRegistry;
import dev.daweapon.type.skyblockgeneric.item.handlers.ability.RegisteredAbility;
import dev.daweapon.type.skyblockgeneric.item.handlers.ability.abilities.AbilityDamage;
import dev.daweapon.type.skyblockgeneric.item.handlers.ability.abilities.ExplosionSound;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.DamageIndicator;

public final class DanteStaff {
    public static final String RAY_OF_HOPE = "RAY_OF_HOPE";
    public static final String RAY_OF_HOPE_GENERAL = "RAY_OF_HOPE_GENERAL";
    public static final double BASE_DAMAGE = 750;
    public static final double INTELLIGENCE_SCALING = 0.2;
    public static final double MANA_PERCENT = 10;
    public static final double GENERAL_MANA_PERCENT = 2;
    public static final double RANGE = 60;
    public static final double BLAST_RADIUS = 4;
    private static final double STEP = 0.5;
    private static final int COOLDOWN_TICKS = 5;

    private DanteStaff() {
    }

    public static void register() {
        AbilityRegistry.register(ray(RAY_OF_HOPE, MANA_PERCENT));
        AbilityRegistry.register(ray(RAY_OF_HOPE_GENERAL, GENERAL_MANA_PERCENT));
    }

    private static RegisteredAbility ray(String id, double manaPercent) {
        return new RegisteredAbility(
                id,
                "Ray of Hope",
                (_, _) -> "<7>Channels your energy towards the target location, exploding on impact for <c>"
                        + (int) BASE_DAMAGE + " <7>damage. Costs <3>" + (int) manaPercent
                        + "% <7>of your total <b>mana <7>to use. <7>This is the only item that can deal damage to <c>Dante<7>.",
                RegisteredAbility.AbilityActivation.RIGHT_CLICK,
                COOLDOWN_TICKS,
                new PercentManaCost(manaPercent),
                (player, _, _, _) -> {
                    cast(player);
                    return true;
                });
    }

    public static void cast(SkyBlockPlayer player) {
        Instance instance = player.getInstance();
        if (instance == null) return;
        Point impact = impactPoint(player, instance);
        instance.sendGroupedPacket(new ParticlePacket(Particle.EXPLOSION, impact, Vec.ZERO, 0, 1));
        ExplosionSound.play(player, impact);
        double damage = AbilityDamage.of(player, BASE_DAMAGE, INTELLIGENCE_SCALING) * ResistanceArmorSet.damageBonus(player);
        for (Entity entity : instance.getNearbyEntities(impact, BLAST_RADIUS + 3)) {
            if (!(entity instanceof SkyBlockMob mob) || mob.isDead()) continue;
            if (!reaches(mob, impact)) continue;
            if (mob instanceof MobDante dante) {
                dante.rayOfHope(player, (float) damage);
                continue;
            }
            mob.damage(new Damage(DamageType.MAGIC, player, player, impact, (float) damage));
            new DamageIndicator().damage((float) damage).pos(mob.getPosition()).critical(false).display(instance);
        }
    }

    static boolean reaches(SkyBlockMob mob, Point impact) {
        Point center = mob.getPosition().add(0, mob.getBoundingBox().height() / 2, 0);
        return center.distance(impact) <= BLAST_RADIUS + mob.getBoundingBox().width() / 2;
    }

    private static Point impactPoint(SkyBlockPlayer player, Instance instance) {
        Pos eye = player.getPosition().add(0, player.getEyeHeight(), 0);
        Vec direction = player.getPosition().direction();
        for (double distance = 1; distance <= RANGE; distance += STEP) {
            Pos probe = eye.add(direction.mul(distance));
            if (instance.getBlock(probe).isSolid() || mobNear(instance, probe)) return probe;
        }
        return eye.add(direction.mul(RANGE));
    }

    private static boolean mobNear(Instance instance, Point probe) {
        for (Entity entity : instance.getNearbyEntities(probe, 8)) {
            if (!(entity instanceof SkyBlockMob mob) || mob.isDead()) continue;
            Point center = mob.getPosition().add(0, mob.getBoundingBox().height() / 2, 0);
            if (center.distance(probe) <= mob.getBoundingBox().width() / 2 + 1.2) return true;
        }
        return false;
    }
}
