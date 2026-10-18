package dev.daweapon.type.skyblockgeneric.utility;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public final class AttackService {
    public static final int EXTRA_HIT_DELAY_TICKS = 5;
    public static final double MAX_MELEE_FEROCITY_DISTANCE = 6D;
    public static final double MAX_FEROCITY = 500D;
    private static final ThreadLocal<Boolean> EXTRA_HIT = ThreadLocal.withInitial(() -> false);

    private AttackService() {
    }

    public static boolean applyHit(SkyBlockPlayer attacker, SkyBlockMob target, float damage, boolean critical) {
        if (target.isRemoved() || target.isDead()) return false;
        damage *= (float) dev.daweapon.type.skyblockgeneric.item.handlers.ability.abilities.BigPull.damageMultiplier(target);

        Instance instance = target.getInstance();
        Pos position = target.getPosition();
        boolean applied = target.damage(new Damage(
                DamageType.PLAYER_ATTACK,
                attacker,
                attacker,
                attacker.getPosition(),
                damage
        ));
        if (!applied) return false;
        dev.daweapon.type.skyblockgeneric.slayer.inferno.TacticalInsertion.recordDamage(attacker, damage);

        String entityName = target.getEntityType().name().toLowerCase(Locale.ROOT).replace("minecraft:", "");
        attacker.playSound(
                Sound.sound(Key.key("entity." + entityName + ".hurt"), Sound.Source.PLAYER, 1f, 1f),
                Sound.Emitter.self()
        );
        if (instance != null) {
            new DamageIndicator()
                    .damage(damage)
                    .pos(position)
                    .critical(critical)
                    .display(instance);
        }
        return true;
    }

    public static void obliterate(SkyBlockPlayer attacker, SkyBlockMob target) {
        if (target.isRemoved() || target.isDead()) return;
        Instance instance = target.getInstance();
        Pos position = target.getPosition();
        float health = target.getHealth();
        target.obliterate(attacker);
        if (instance != null) {
            new DamageIndicator().damage(health).pos(position).critical(true).display(instance);
        }
    }

    public static void scheduleExtraHits(SkyBlockPlayer attacker, SkyBlockMob target,
                                         float damage, boolean critical, double ferocity) {
        double tempo = dev.daweapon.type.skyblockgeneric.enchantment.impl.EnchantmentFatalTempo.registerHit(attacker);
        double roll = ThreadLocalRandom.current().nextDouble(100D);
        int regularHits = calculateExtraHits(ferocity, roll);
        int extraHits = calculateExtraHits(ferocity + tempo, roll);
        if (extraHits > 0) dev.daweapon.type.skyblockgeneric.item.armor.ArmorAbilityEffects.onFerocityActivated(attacker);

        for (int hit = 0; hit < extraHits; hit++) {
            int delay = EXTRA_HIT_DELAY_TICKS * (hit + 1);
            boolean regular = hit < regularHits;
            MinecraftServer.getSchedulerManager().scheduleTask(
                    () -> {
                        EXTRA_HIT.set(true);
                        try {
                            if (applyHit(attacker, target, damage, critical) && regular)
                                dev.daweapon.type.skyblockgeneric.enchantment.impl.EnchantmentFatalTempo.registerHit(attacker);
                        } finally {
                            EXTRA_HIT.set(false);
                        }
                    },
                    TaskSchedule.tick(delay),
                    TaskSchedule.stop()
            );
        }
    }

    public static boolean isExtraHit() {
        return EXTRA_HIT.get();
    }

    static int calculateExtraHits(double ferocity, double chanceRoll) {
        double effectiveFerocity = Math.max(0D, Math.min(MAX_FEROCITY, ferocity));
        int extraHits = (int) (effectiveFerocity / 100D);
        double remainingChance = effectiveFerocity % 100D;
        if (remainingChance > 0D && chanceRoll < remainingChance) {
            extraHits++;
        }
        return extraHits;
    }
}
