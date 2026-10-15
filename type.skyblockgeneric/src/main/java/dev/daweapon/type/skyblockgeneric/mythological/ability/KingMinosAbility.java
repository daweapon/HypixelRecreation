package dev.daweapon.type.skyblockgeneric.mythological.ability;

import net.minestom.server.color.DyeColor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.particle.Particle;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.generic.entity.ai.vanilla.VanillaNavigator;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.mythological.MythologicalCreatureMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.MobAttackService;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * King Minos fights in two phases, halving the Vitality of everyone fighting him.
 *
 * <p>Shield phase: he rides an orange sheep behind a 75-hit shield, ramming players into the air
 * and casting a fishing rod that roots whoever it hooks. Breaking the shield dismounts him.
 *
 * <p>Damage phase: Rage climbs 1% each time he deals or takes damage, up to 100%, and turns that
 * share of his damage into true damage. From 30% Rage he unleashes an area attack around himself.
 * He calls lightning onto the player's block, and draws a bow at players out of reach.
 */
final class KingMinosAbility implements MythologicalAbility {
    private static final String PRESENCE = "King Minos' Presence";
    private static final int SHIELD_HITS = 75;
    private static final double FIGHT_RANGE = 30;
    private static final double VITALITY_REDUCTION = -50;

    private static final double SHEEP_SPEED = 0.3;
    private static final int SHEEP_REPATH_TICKS = 10;
    private static final int RAM_INTERVAL_TICKS = 80;
    private static final double RAM_SPEED = 20;
    private static final int RAM_MAX_TICKS = 20;
    private static final double RAM_MIN_DISTANCE = 2.5;
    private static final double RAM_MAX_DISTANCE = 14;
    private static final double RAM_CONTACT_DISTANCE = 2.2;
    private static final double RAM_LAUNCH = 30;
    private static final int ROD_INTERVAL_TICKS = 140;
    private static final int FIRST_ROD_TICKS = 70;
    private static final double ROD_SPEED = 1.2;
    private static final double ROD_RANGE = 20;
    private static final int ROOT_TICKS = 60;
    private static final double DEFAULT_JUMP_STRENGTH = 0.42;

    private static final int ENRAGED_AT = 30;
    private static final int MAX_RAGE = 100;
    private static final int LIGHTNING_INTERVAL_TICKS = 100;
    private static final int LIGHTNING_DELAY_TICKS = 20;
    private static final double LIGHTNING_RADIUS = 1.5;
    private static final double LIGHTNING_DAMAGE_MULTIPLIER = 2;
    private static final int AREA_INTERVAL_TICKS = 120;
    private static final int AREA_WIND_UP_TICKS = 20;
    private static final double AREA_RADIUS = 6;
    private static final double AREA_DAMAGE_MULTIPLIER = 3;
    private static final double BOW_MIN_DISTANCE = 8;
    private static final double BOW_MAX_DISTANCE = 30;
    private static final int BOW_INTERVAL_TICKS = 25;
    private static final double ARROW_SPEED = 1.8;

    private final Map<UUID, Integer> rootedUntil = new HashMap<>();
    private final Set<UUID> weakened = new HashSet<>();
    private EntityCreature sheep;
    private VanillaNavigator sheepNavigator;
    private int shieldHits = SHIELD_HITS;
    private int rage;
    private Vec ramDirection;
    private int ramTicks;
    private Pos lightningAt;
    private int lightningStrikesAt = -1;
    private int areaStrikesAt = -1;

    private boolean shielded() {
        return shieldHits > 0;
    }

    @Override
    public void onSpawn(MythologicalCreatureMob mob) {
        mob.setAiPaused(true);
        sheep = new EntityCreature(EntityType.SHEEP);
        sheep.editEntityMeta(SheepMeta.class, meta -> meta.setColor(DyeColor.ORANGE));
        sheep.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(SHEEP_SPEED);
        sheep.setInvulnerable(true);
        sheep.setInstance(mob.getInstance(), mob.getPosition());
        sheepNavigator = new VanillaNavigator(sheep);
    }

    @Override
    public void tick(MythologicalCreatureMob mob, int ticksAlive) {
        if (ticksAlive % 20 == 0) {
            weakenFighters(mob);
            mob.refreshName();
        }
        releaseRooted(ticksAlive);
        SkyBlockPlayer target = MythologicalCombat.target(mob).orElse(null);
        if (shielded()) {
            tickMounted(mob, target, ticksAlive);
        } else {
            tickEnraged(mob, target, ticksAlive);
        }
    }

    private void tickMounted(MythologicalCreatureMob mob, SkyBlockPlayer target, int ticksAlive) {
        if (sheep.getInstance() == null) return;
        if (mob.getVehicle() != sheep) sheep.addPassenger(mob);
        sheepNavigator.tick();
        if (target == null) return;
        double distance = target.getPosition().distance(sheep.getPosition());

        if (ramDirection != null) {
            sheep.setVelocity(ramDirection.mul(RAM_SPEED).withY(sheep.getVelocity().y()));
            if (distance <= RAM_CONTACT_DISTANCE) {
                ramDirection = null;
                sheep.setVelocity(Vec.ZERO);
                MobAttackService.strike(mob, target);
                MythologicalCombat.launch(target, new Vec(0, RAM_LAUNCH, 0));
                MythologicalCombat.sound(sheep, "entity.sheep.hurt", 1.2f, 0.5f);
            } else if (++ramTicks >= RAM_MAX_TICKS) {
                ramDirection = null;
            }
            return;
        }
        if (ticksAlive % RAM_INTERVAL_TICKS == 0 && distance >= RAM_MIN_DISTANCE && distance <= RAM_MAX_DISTANCE) {
            sheepNavigator.stop();
            sheep.lookAt(target);
            ramDirection = target.getPosition().sub(sheep.getPosition()).asVec().withY(0).normalize();
            ramTicks = 0;
            MythologicalCombat.sound(sheep, "entity.ravager.roar", 0.8f, 1.6f);
            return;
        }
        if (ticksAlive >= FIRST_ROD_TICKS && (ticksAlive - FIRST_ROD_TICKS) % ROD_INTERVAL_TICKS == 0
                && distance <= ROD_RANGE) {
            mob.lookAt(target);
            MythologicalCombat.sound(mob, "entity.fishing_bobber.throw", 1f, 0.8f);
            MobProjectile.bobber(mob, target, ROD_SPEED, player -> root(mob, player));
        }
        if (ticksAlive % SHEEP_REPATH_TICKS == 0) sheepNavigator.pathTo(target.getPosition());
    }

    private void root(MythologicalCreatureMob mob, SkyBlockPlayer player) {
        rootedUntil.put(player.getUuid(), mob.getFightTicks() + ROOT_TICKS);
        player.setSpeedManaged(true);
        player.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0);
        player.getAttribute(Attribute.JUMP_STRENGTH).setBaseValue(0);
        player.sendMessage("<c>You were hooked by <5>King Minos<c>!");
    }

    private void releaseRooted(int ticksAlive) {
        for (Map.Entry<UUID, Integer> entry : Map.copyOf(rootedUntil).entrySet()) {
            if (ticksAlive >= entry.getValue()) release(entry.getKey());
        }
    }

    private void release(UUID id) {
        rootedUntil.remove(id);
        SkyBlockPlayer player = SkyBlockGenericLoader.getFromUUID(id);
        if (player == null) return;
        player.getAttribute(Attribute.JUMP_STRENGTH).setBaseValue(DEFAULT_JUMP_STRENGTH);
        player.setSpeedManaged(false);
    }

    private void tickEnraged(MythologicalCreatureMob mob, SkyBlockPlayer target, int ticksAlive) {
        if (areaStrikesAt >= 0) {
            if (ticksAlive % 4 == 0) {
                MythologicalCombat.particles(mob.getInstance(), Particle.ANGRY_VILLAGER, mob.getPosition().add(0, 1, 0),
                        (float) AREA_RADIUS / 2, 12);
            }
            if (ticksAlive >= areaStrikesAt) {
                areaStrikesAt = -1;
                MythologicalCombat.particles(mob.getInstance(), Particle.EXPLOSION, mob.getPosition().add(0, 1, 0),
                        (float) AREA_RADIUS / 2, 10);
                MythologicalCombat.sound(mob, "entity.generic.explode", 1f, 0.8f);
                for (SkyBlockPlayer player : MythologicalCombat.nearby(mob, AREA_RADIUS)) {
                    strikeWithRage(mob, player, baseDamage(mob) * AREA_DAMAGE_MULTIPLIER);
                }
            }
        } else if (rage >= ENRAGED_AT && ticksAlive % AREA_INTERVAL_TICKS == 0) {
            areaStrikesAt = ticksAlive + AREA_WIND_UP_TICKS;
        }

        if (ticksAlive == lightningStrikesAt && lightningAt != null) {
            MythologicalCombat.lightning(mob.getInstance(), lightningAt);
            for (SkyBlockPlayer player : MythologicalCombat.nearby(mob, FIGHT_RANGE)) {
                if (player.getPosition().distance(lightningAt) <= LIGHTNING_RADIUS) {
                    strikeWithRage(mob, player, baseDamage(mob) * LIGHTNING_DAMAGE_MULTIPLIER);
                }
            }
            lightningAt = null;
        }

        if (target == null) return;
        if (ticksAlive % LIGHTNING_INTERVAL_TICKS == 0) {
            lightningAt = target.getPosition();
            lightningStrikesAt = ticksAlive + LIGHTNING_DELAY_TICKS;
            MythologicalCombat.particles(mob.getInstance(), Particle.ELECTRIC_SPARK, lightningAt.add(0, 0.2, 0), 0.5f, 25);
        }
        double distance = target.getPosition().distance(mob.getPosition());
        if (ticksAlive % BOW_INTERVAL_TICKS == 0 && distance >= BOW_MIN_DISTANCE && distance <= BOW_MAX_DISTANCE) {
            mob.lookAt(target);
            MythologicalCombat.sound(mob, "entity.skeleton.shoot", 1f, 0.9f);
            MobProjectile.arrow(mob, target, ARROW_SPEED, player -> strikeWithRage(mob, player, baseDamage(mob)));
        }
    }

    private double baseDamage(MythologicalCreatureMob mob) {
        return mob.getVariant().damage();
    }

    private void strikeWithRage(MythologicalCreatureMob mob, SkyBlockPlayer player, double damage) {
        double trueShare = damage * rage / 100D;
        MobAttackService.strike(mob, player, damage - trueShare);
        MobAttackService.trueStrike(mob, player, trueShare);
        addRage(mob);
    }

    private void addRage(MythologicalCreatureMob mob) {
        if (shielded() || rage >= MAX_RAGE) return;
        rage++;
        mob.refreshName();
    }

    @Override
    public void onMeleeHit(MythologicalCreatureMob mob, SkyBlockPlayer target) {
        if (shielded()) return;
        MobAttackService.trueStrike(mob, target, baseDamage(mob) * rage / 100D);
        addRage(mob);
    }

    @Override
    public ItemStatistics modifyStatistics(MythologicalCreatureMob mob, ItemStatistics statistics) {
        if (rage <= 0) return statistics;
        return ItemStatistics.add(statistics, ItemStatistics.builder()
                .withBase(ItemStatistic.DAMAGE, -baseDamage(mob) * rage / 100D)
                .build());
    }

    @Override
    public float modifyIncomingDamage(MythologicalCreatureMob mob, Damage damage, float amount) {
        if (!shielded()) return amount;
        if (!(damage.getAttacker() instanceof SkyBlockPlayer)) return 0;
        shieldHits--;
        MythologicalCombat.sound(mob, "block.amethyst_block.hit", 1f, 0.6f);
        if (!shielded()) dismount(mob);
        mob.refreshName();
        return 0;
    }

    @Override
    public void onDamaged(MythologicalCreatureMob mob, Damage damage, float amount) {
        addRage(mob);
    }

    private void dismount(MythologicalCreatureMob mob) {
        if (sheep != null) {
            sheep.removePassenger(mob);
            MythologicalCombat.particles(sheep.getInstance(), Particle.POOF, sheep.getPosition().add(0, 0.5, 0), 0.5f, 20);
            sheep.remove();
        }
        mob.setAiPaused(false);
        MythologicalCombat.sound(mob, "entity.wither.break_block", 1f, 1f);
    }

    private void weakenFighters(MythologicalCreatureMob mob) {
        List<SkyBlockPlayer> fighters = MythologicalCombat.nearby(mob, FIGHT_RANGE);
        Set<UUID> present = new HashSet<>();
        for (SkyBlockPlayer player : fighters) {
            present.add(player.getUuid());
            if (weakened.add(player.getUuid())) {
                MythologicalCombat.debuff(player, PRESENCE, ItemStatistics.builder()
                        .withMultiplicativePercentage(ItemStatistic.VITALITY, VITALITY_REDUCTION)
                        .build());
            }
        }
        for (UUID id : Set.copyOf(weakened)) {
            if (!present.contains(id)) unweaken(id);
        }
    }

    private void unweaken(UUID id) {
        weakened.remove(id);
        SkyBlockPlayer player = SkyBlockGenericLoader.getFromUUID(id);
        if (player != null) MythologicalCombat.clearDebuff(player, PRESENCE);
    }

    @Override
    public String nameHeader(MythologicalCreatureMob mob) {
        if (shielded()) return "<e>" + shieldHits + " Hits";
        return (rage >= ENRAGED_AT ? "<c>" : "<a>") + "Rage " + rage + "%";
    }

    @Override
    public void cleanup(MythologicalCreatureMob mob) {
        if (sheep != null && !sheep.isRemoved()) sheep.remove();
        Map.copyOf(rootedUntil).keySet().forEach(this::release);
        Set.copyOf(weakened).forEach(this::unweaken);
    }
}
