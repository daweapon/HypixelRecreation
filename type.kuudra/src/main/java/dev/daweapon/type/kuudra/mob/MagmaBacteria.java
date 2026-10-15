package dev.daweapon.type.kuudra.mob;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.type.kuudra.fight.KuudraFight;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraMobSpec;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.mythological.ability.MobProjectile;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Comparator;
import java.util.List;

/**
 * Lives only in Kuudra's stomach (research K1 5.3): stands still and fires projectiles that knock players back
 * and off the pods. Crowd Control's Antibiotic cuts both the damage and the knockback. Hypixel draws it as a
 * player; here it is a zombie with a magma head.
 */
public final class MagmaBacteria extends KuudraMob {
    public static final String ID = "MAGMA_BACTERIA";

    public MagmaBacteria(KuudraFight fight, KuudraMobSpec spec) {
        super(fight, spec, EntityType.ZOMBIE, MobType.INFERNAL);
        setEquipment(EquipmentSlot.HELMET, ItemStack.of(Material.MAGMA_BLOCK));
        setEquipment(EquipmentSlot.CHESTPLATE, ItemStack.of(Material.NETHERITE_CHESTPLATE));
    }

    @Override
    public List<GoalSelector> getGoalSelectors() {
        return List.of();
    }

    @Override
    public List<TargetSelector> getTargetSelectors() {
        return List.of();
    }

    @Override
    protected int strollRadius() {
        return 0;
    }

    @Override
    protected boolean melee() {
        return false;
    }

    @Override
    protected boolean canTarget(SkyBlockPlayer player) {
        return fight.isInStomach(player) && fight.isTargetable(player);
    }

    /** Antibiotic's share taken off a Magma Bacteria hit or push (30% to 90%). */
    public static double reduced(double amount, double antibiotic) {
        return amount * (1 - Math.min(1, Math.max(0, antibiotic)));
    }

    @Override
    protected void behave(int ticks) {
        var stomach = fight.spec().stomach();
        if (ticks % (stomach.bacteriaFireSeconds() * 20) != 0) return;
        fight.stomachPlayers().stream()
                .filter(player -> player.getPosition().distance(getPosition()) <= stomach.bacteriaRange())
                .min(Comparator.comparingDouble(player -> player.getPosition().distanceSquared(getPosition())))
                .ifPresent(target -> MobProjectile.item(this, target, ItemStack.of(Material.MAGMA_CREAM), 0.8, hit -> {
                    double antibiotic = fight.perks().fraction(hit.getUuid(), "ANTIBIOTIC");
                    trueStrike(hit, reduced(damageStat(), antibiotic));
                    Vec away = hit.getPosition().sub(getPosition()).asVec().withY(0);
                    if (away.lengthSquared() > 1e-6) {
                        hit.setVelocity(away.normalize().mul(reduced(stomach.bacteriaKnockback(), antibiotic))
                                .withY(reduced(stomach.bacteriaKnockback() / 2, antibiotic)));
                    }
                }));
    }
}
