package dev.daweapon.type.catacombs.classes;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.dungeons.catacombs.classes.DungeonClassScaling;
import dev.daweapon.dungeons.catacombs.classes.DungeonClassType;
import dev.daweapon.type.catacombs.run.CatacombsRun;
import dev.daweapon.type.catacombs.run.CatacombsRuns;
import dev.daweapon.type.skyblockgeneric.dungeons.DungeonClassHooks;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.AttackService;
import net.minestom.server.entity.damage.EntityDamage;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The class passives that reach into SkyBlock combat: Berserk and Archer damage bonuses, Bloodlust, Doubleshot, Tank's
 * Diversion and Castle of Stone, Mage's Efficient Spells on item abilities and the Mage Staff beam.
 */
public final class ClassCombat implements DungeonClassHooks.Handler {
    private static final long MELEE_GRACE_MILLIS = 150;
    private static final long BEAM_INTERVAL_MILLIS = 500;
    private static final Map<UUID, Long> LAST_MELEE = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> LAST_BEAM = new ConcurrentHashMap<>();

    public static void install() {
        DungeonClassHooks.install(new ClassCombat());
    }

    static @Nullable RunClasses active(SkyBlockPlayer player) {
        CatacombsRun run = CatacombsRuns.of(player.getUuid());
        if (run == null) return null;
        RunClasses classes = run.classes();
        if (!classes.host().classesActive() || !classes.inParty(player.getUuid()) || run.isGhost(player.getUuid())) return null;
        return classes;
    }

    @Override
    public double meleeMultiplier(SkyBlockPlayer player, SkyBlockMob mob) {
        RunClasses classes = active(player);
        if (classes == null) return 1;
        UUID uuid = player.getUuid();
        return switch (classes.classOf(uuid)) {
            case BERSERK -> (1 + DungeonClassScaling.berserkMeleePercent(classes.level(uuid), classes.strength(uuid)) / 100)
                    * (classes.inRagnarok(uuid, System.currentTimeMillis()) ? DungeonClassScaling.RAGNAROK_MELEE_MULTIPLIER : 1);
            case ARCHER -> 1 - DungeonClassScaling.ARCHER_MELEE_PENALTY_PERCENT / 100.0;
            default -> 1;
        };
    }

    @Override
    public void meleeHit(SkyBlockPlayer player, SkyBlockMob mob, double damage) {
        LAST_MELEE.put(player.getUuid(), System.currentTimeMillis());
        RunClasses classes = active(player);
        if (classes == null) return;
        classes.meleeHits().record(player.getUuid(), damage, System.currentTimeMillis());
        if (classes.classOf(player.getUuid()) == DungeonClassType.BERSERK) {
            double missing = player.getMaxHealth() - player.getHealth();
            classes.abilities().heal(player, missing * DungeonClassScaling.BLOODLUST_MISSING_HEALTH_PERCENT / 100, null);
        }
    }

    @Override
    public double arrowMultiplier(SkyBlockPlayer player, SkyBlockMob mob) {
        RunClasses classes = active(player);
        if (classes == null || classes.classOf(player.getUuid()) != DungeonClassType.ARCHER) return 1;
        return 1 + DungeonClassScaling.archerArrowPercent(classes.level(player.getUuid()), classes.strength(player.getUuid())) / 100;
    }

    @Override
    public void arrowHit(SkyBlockPlayer player, SkyBlockMob mob, double damage) {
        RunClasses classes = active(player);
        if (classes == null) return;
        long now = System.currentTimeMillis();
        classes.bowHits().record(player.getUuid(), damage, now);
        classes.meleeHits().record(player.getUuid(), damage, now);
    }

    @Override
    public int extraArrows(SkyBlockPlayer player) {
        RunClasses classes = active(player);
        if (classes == null || classes.classOf(player.getUuid()) != DungeonClassType.ARCHER) return 0;
        double chance = DungeonClassScaling.doubleshotChancePercent(classes.level(player.getUuid()), classes.strength(player.getUuid()));
        return ThreadLocalRandom.current().nextDouble(100) < chance ? 1 : 0;
    }

    @Override
    public float incomingDamage(SkyBlockPlayer player, SkyBlockMob mob, float damage) {
        CatacombsRun run = CatacombsRuns.of(player.getUuid());
        if (run == null || !run.classes().host().classesActive()) return damage;
        RunClasses classes = run.classes();
        long now = System.currentTimeMillis();
        float taken = castle(classes, player.getUuid(), damage, now);
        if (classes.classOf(player.getUuid()) == DungeonClassType.TANK) return taken;
        SkyBlockPlayer tank = run.alivePlayers().stream()
                .filter(mate -> mate != player && classes.inParty(mate.getUuid()) && classes.classOf(mate.getUuid()) == DungeonClassType.TANK)
                .filter(mate -> mate.getPosition().distance(player.getPosition()) <= DungeonClassScaling.DIVERSION_RADIUS)
                .min(Comparator.comparingDouble(mate -> mate.getPosition().distance(player.getPosition())))
                .orElse(null);
        if (tank == null) return taken;
        float diverted = damage * DungeonClassScaling.DIVERSION_PERCENT / 100f;
        tank.damage(new EntityDamage(mob, castle(classes, tank.getUuid(), diverted, now)));
        return castle(classes, player.getUuid(), damage - diverted, now);
    }

    private static float castle(RunClasses classes, UUID player, float damage, long now) {
        return classes.inCastle(player, now) ? damage * (100 - DungeonClassScaling.CASTLE_OF_STONE_REDUCTION_PERCENT) / 100f : damage;
    }

    @Override
    public int cooldownTicks(SkyBlockPlayer player, int ticks) {
        RunClasses classes = active(player);
        if (classes == null || classes.classOf(player.getUuid()) != DungeonClassType.MAGE) return ticks;
        return (int) Math.round(ticks * (1 - DungeonClassScaling.mageCooldownReductionPercent(classes.level(player.getUuid())) / 100));
    }

    /** Mage Staff: a left click with a melee weapon that hit nothing fires a beam at the first enemy within range. */
    public static void mageStaff(SkyBlockPlayer player) {
        RunClasses classes = active(player);
        if (classes == null || classes.classOf(player.getUuid()) != DungeonClassType.MAGE) return;
        if (!isMeleeWeapon(player.getItemInMainHand().material())) return;
        long now = System.currentTimeMillis();
        if (now - LAST_MELEE.getOrDefault(player.getUuid(), 0L) < MELEE_GRACE_MILLIS) return;
        if (now - LAST_BEAM.getOrDefault(player.getUuid(), 0L) < BEAM_INTERVAL_MILLIS) return;
        LAST_BEAM.put(player.getUuid(), now);
        ClassTargeting.Hit hit = ClassTargeting.traceMob(player, DungeonClassScaling.MAGE_STAFF_RANGE, 0);
        ClassTargeting.trail(player.getInstance(), ClassTargeting.eye(player), hit.point(), net.minestom.server.particle.Particle.END_ROD);
        if (!(hit.entity() instanceof SkyBlockMob mob)) return;
        var result = player.getStatistics().runPrimaryDamageFormula(mob.getStatistics(), player, mob);
        double intelligence = player.getStatistics().allStatistics().getOverall(ItemStatistic.INTELLIGENCE);
        float damage = (float) (result.getKey() * DungeonClassScaling.mageStaffFactor(intelligence));
        if (AttackService.applyHit(player, mob, damage, result.getValue())) {
            classes.meleeHits().record(player.getUuid(), damage, now);
        }
    }

    private static boolean isMeleeWeapon(Material material) {
        String key = material.key().value();
        return key.endsWith("_sword") || key.endsWith("_axe");
    }
}
