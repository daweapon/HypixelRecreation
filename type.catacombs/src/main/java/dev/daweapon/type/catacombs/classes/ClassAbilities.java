package dev.daweapon.type.catacombs.classes;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.dungeons.catacombs.classes.DungeonClassAbility;
import dev.daweapon.dungeons.catacombs.classes.DungeonClassAbilityType;
import dev.daweapon.dungeons.catacombs.classes.DungeonClassRegistry;
import dev.daweapon.dungeons.catacombs.classes.DungeonClassScaling;
import dev.daweapon.dungeons.catacombs.classes.DungeonClassType;
import dev.daweapon.dungeons.catacombs.mob.DungeonMobRole;
import dev.daweapon.type.catacombs.engine.CatacombsMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobStatusEffects;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.statistics.TemporaryStatistic;
import dev.daweapon.type.skyblockgeneric.utility.AttackService;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.particle.Particle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Every class ability in a run: the Dungeon Orb's normal and ultimate abilities, the ghost abilities and Haunt, and
 * the per-second passives (Healing Aura, Healing Circles, Thunderstorm). Numbers come from {@link DungeonClassScaling}.
 */
public final class ClassAbilities {
    private static final DungeonClassRegistry REGISTRY = DungeonClassRegistry.defaults();
    private static final double AXE_RANGE = 20;
    private static final double BOW_RANGE = 40;
    private static final double TARGET_RANGE = 20;
    private static final double SEISMIC_LENGTH = 12;
    private static final double SEISMIC_WIDTH = 1.5;
    private static final double SHEEP_RANGE = 30;
    private static final double SHEEP_RADIUS = 3;
    private static final double FIREBALL_RADIUS = 3;
    private static final double EXPLOSIVE_SPREAD_DEGREES = 5;
    private static final int THUNDERSTORM_STRIKES_PER_SECOND = 3;
    private static final int RAPID_FIRE_INTERVAL_TICKS = 20 / DungeonClassScaling.RAPID_FIRE_ARROWS_PER_SECOND;
    private static final int HAUNT_COOLDOWN = 2;
    private static final double OVERHEAL_BASE_PERCENT = 20;
    private static final double OVERHEAL_PER_LEVEL = 0.4;

    private record HealingCircle(UUID healer, Point center, double radius, double percent, long until) {
    }

    private record Thunderstorm(UUID mage, long until) {
    }

    private final RunClasses classes;
    private final List<HealingCircle> circles = new CopyOnWriteArrayList<>();
    private final List<Thunderstorm> storms = new CopyOnWriteArrayList<>();
    private final Map<UUID, Integer> hauntIndex = new ConcurrentHashMap<>();
    private volatile boolean stopped;

    ClassAbilities(RunClasses classes) {
        this.classes = classes;
    }

    public void orb(SkyBlockPlayer player, boolean ultimate) {
        UUID uuid = player.getUuid();
        if (!usable(player) || classes.host().isGhost(uuid)) return;
        DungeonClassType type = classes.classOf(uuid);
        DungeonClassAbility ability = REGISTRY.ability(type, ultimate ? DungeonClassAbilityType.ULTIMATE : DungeonClassAbilityType.ORB, 0);
        if (!startCooldown(player, ability, DungeonClassScaling.cooldownSeconds(ability, type, classes.level(uuid)))) return;
        switch (ability.id()) {
            case "HEALING_CIRCLE" -> healingCircle(player);
            case "WISH" -> wish(player);
            case "GUIDED_SHEEP" -> guidedSheep(player);
            case "THUNDERSTORM" -> storms.add(new Thunderstorm(uuid, now() + DungeonClassScaling.THUNDERSTORM_SECONDS * 1000L));
            case "THROWING_AXE" -> throwingAxe(player);
            case "RAGNAROK" -> ragnarok(player);
            case "EXPLOSIVE_SHOT" -> explosiveShot(player);
            case "RAPID_FIRE" -> rapidFire(player, DungeonClassScaling.rapidFireSeconds(classes.level(uuid)) * 20 / RAPID_FIRE_INTERVAL_TICKS);
            case "SEISMIC_WAVE" -> seismicWave(player);
            case "CASTLE_OF_STONE" -> castleOfStone(player);
            default -> {
            }
        }
        player.sendMessage("<a>Used <6>" + ability.displayName() + "<a>!");
    }

    public void ghost(SkyBlockPlayer player, int index) {
        UUID uuid = player.getUuid();
        if (!usable(player) || !classes.host().isGhost(uuid)) return;
        DungeonClassType type = classes.classOf(uuid);
        DungeonClassAbility ability = REGISTRY.ability(type, DungeonClassAbilityType.GHOST, index);
        if (!startCooldown(player, ability, ability.cooldownSeconds())) return;
        int level = classes.level(uuid);
        int strength = classes.strength(uuid);
        switch (ability.id()) {
            case "HEALING_POTION" -> teammatesNear(player, DungeonClassScaling.GHOST_POTION_RADIUS).forEach(mate -> heal(mate,
                    mate.getMaxHealth() * DungeonClassScaling.ghostHealingPotionPercent(level, strength) / 100
                            + DungeonClassScaling.ghostHealingPotionFlat(level, strength), null));
            case "REVIVE_SELF" -> {
                int seconds = DungeonClassScaling.selfReviveSeconds(level);
                classes.host().reviveAfter(uuid, seconds);
                player.sendMessage("<a>You will revive yourself in <e>" + seconds + " <a>seconds.");
            }
            case "INSTANT_WALL" -> instantWall(player);
            case "FIREBALL" -> fireball(player, DungeonClassScaling.fireballDamage(level, strength));
            case "BUFF_POTION" -> teammatesNear(player, DungeonClassScaling.GHOST_POTION_RADIUS).forEach(mate ->
                    buff(mate, "Buff Potion", ItemStatistic.STRENGTH, DungeonClassScaling.buffPotionStrength(level, strength),
                            DungeonClassScaling.BUFF_POTION_SECONDS));
            case "GHOST_AXE" -> axe(player, DungeonClassScaling.ghostAxeDamage(level, strength));
            case "STUN_BOW" -> stunBow(player);
            case "HEALING_BOW" -> healingBow(player);
            case "STUN_POTION" -> ClassTargeting.mobsNear(player.getInstance(), player.getPosition(), DungeonClassScaling.GHOST_POTION_RADIUS)
                    .stream().filter(ClassAbilities::stunnable)
                    .forEach(mob -> MobStatusEffects.stun(mob, DungeonClassScaling.STUN_SECONDS * 1000L));
            case "ABSORPTION_POTION" -> teammatesNear(player, DungeonClassScaling.GHOST_POTION_RADIUS).forEach(mate ->
                    absorption(mate, (float) DungeonClassScaling.absorptionPotion(level, strength), DungeonClassScaling.ABSORPTION_POTION_SECONDS));
            default -> {
            }
        }
        player.sendMessage("<a>Used <f>" + ability.displayName() + "<a>!");
    }

    public void haunt(SkyBlockPlayer player) {
        if (!usable(player) || !classes.host().isGhost(player.getUuid())) return;
        List<SkyBlockPlayer> alive = classes.host().alivePlayers();
        if (alive.isEmpty()) {
            player.sendMessage("<c>There is no one alive to haunt!");
            return;
        }
        if (!classes.cooldowns().tryUse(player.getUuid(), "HAUNT", HAUNT_COOLDOWN, now())) return;
        int next = Math.floorMod(hauntIndex.merge(player.getUuid(), 1, Integer::sum), alive.size());
        SkyBlockPlayer target = alive.get(next);
        player.teleport(target.getPosition());
        player.sendMessage("<a>You are now haunting <e>" + target.getUsername() + "<a>.");
    }

    private boolean usable(SkyBlockPlayer player) {
        return !stopped && classes.host().classesActive() && classes.inParty(player.getUuid()) && player.getInstance() == classes.host().instance();
    }

    private boolean startCooldown(SkyBlockPlayer player, DungeonClassAbility ability, double seconds) {
        if (classes.cooldowns().tryUse(player.getUuid(), ability.id(), seconds, now())) return true;
        long left = classes.cooldowns().remainingMillis(player.getUuid(), ability.id(), now());
        player.sendMessage("<c>This ability is on cooldown for " + dev.daweapon.dungeons.catacombs.classes.AbilityCooldowns.displaySeconds(left) + "s.");
        return false;
    }

    private void healingCircle(SkyBlockPlayer player) {
        UUID uuid = player.getUuid();
        int level = classes.level(uuid);
        Point center = ClassTargeting.trace(player, TARGET_RANGE, 0, _ -> false).point();
        while (!player.getInstance().getBlock(center.sub(0, 1, 0)).isSolid() && center.y() > player.getPosition().y() - 10) {
            center = center.sub(0, 1, 0);
        }
        ClassTargeting.trail(player.getInstance(), ClassTargeting.eye(player), center, Particle.HEART);
        circles.add(new HealingCircle(uuid, center, DungeonClassScaling.healingCircleRadius(level),
                DungeonClassScaling.healingCirclePercent(level, classes.strength(uuid)),
                now() + DungeonClassScaling.healingCircleSeconds(level) * 1000L));
    }

    private void wish(SkyBlockPlayer healer) {
        double shieldPercent = DungeonClassScaling.wishShieldPercent(classes.level(healer.getUuid()), classes.strength(healer.getUuid()));
        for (SkyBlockPlayer mate : classes.host().alivePlayers()) {
            mate.setHealth(mate.getMaxHealth());
            mate.setAdditionalHearts(Math.max(mate.getAdditionalHearts(), (float) (mate.getMaxHealth() * shieldPercent / 100)));
            if (mate != healer) mate.sendMessage("<a>" + healer.getUsername() + "'s Wish healed you for " + Math.round(mate.getMaxHealth()) + " health!");
        }
    }

    private void guidedSheep(SkyBlockPlayer player) {
        UUID uuid = player.getUuid();
        double damage = DungeonClassScaling.guidedSheepDamage(classes.level(uuid), classes.strength(uuid));
        Instance instance = player.getInstance();
        Entity sheep = new Entity(EntityType.SHEEP);
        sheep.setNoGravity(true);
        Vec direction = ClassTargeting.direction(player.getPosition(), 0);
        Pos start = ClassTargeting.eye(player).add(direction);
        sheep.setInstance(instance, start);
        flySheep(player, sheep, direction, damage, 0);
    }

    private void flySheep(SkyBlockPlayer player, Entity sheep, Vec direction, double damage, int travelled) {
        if (stopped || sheep.isRemoved()) return;
        Pos next = sheep.getPosition().add(direction);
        boolean blocked = sheep.getInstance().getBlock(next).isSolid();
        boolean touching = !ClassTargeting.mobsNear(sheep.getInstance(), next, 1.5).isEmpty();
        if (blocked || touching || travelled >= SHEEP_RANGE) {
            explode(player, sheep.getPosition(), SHEEP_RADIUS, damage);
            sheep.remove();
            return;
        }
        sheep.teleport(next.withDirection(direction));
        classes.host().later(1, () -> flySheep(player, sheep, direction, damage, travelled + 1));
    }

    private void throwingAxe(SkyBlockPlayer player) {
        UUID uuid = player.getUuid();
        double highest = classes.meleeHits().highest(uuid, now());
        if (highest <= 0) {
            player.sendMessage("<c>You haven't hit anything in the last minute!");
            return;
        }
        axe(player, highest * DungeonClassScaling.throwingAxeMultiplier(classes.level(uuid), classes.strength(uuid)));
    }

    private void axe(SkyBlockPlayer player, double damage) {
        ClassTargeting.Hit hit = ClassTargeting.traceMob(player, AXE_RANGE, 0);
        ClassTargeting.trail(player.getInstance(), ClassTargeting.eye(player), hit.point(), Particle.CRIT);
        if (hit.entity() instanceof SkyBlockMob mob) AttackService.applyHit(player, mob, (float) damage, false);
    }

    private void ragnarok(SkyBlockPlayer player) {
        int seconds = DungeonClassScaling.RAGNAROK_SECONDS;
        classes.ragnarok(player.getUuid(), now() + seconds * 1000L);
        player.getStatistics().boostStatistic(TemporaryStatistic.builder()
                .withStatistics(ItemStatistics.builder()
                        .withBase(ItemStatistic.BONUS_ATTACK_SPEED, (double) DungeonClassScaling.RAGNAROK_ATTACK_SPEED)
                        .withBase(ItemStatistic.SPEED, (double) DungeonClassScaling.RAGNAROK_SPEED)
                        .build())
                .withExpirationInMs(seconds * 1000L)
                .withDisplayName("Ragnarok")
                .withDisplayColor("<c>")
                .build());
    }

    private void explosiveShot(SkyBlockPlayer player) {
        double damage = classes.bowHits().highest(player.getUuid(), now());
        if (damage <= 0) {
            player.sendMessage("<c>You haven't hit anything with a bow in the last minute!");
            return;
        }
        for (int arrow = 0; arrow < DungeonClassScaling.EXPLOSIVE_SHOT_ARROWS; arrow++) {
            double yaw = (arrow - 1) * EXPLOSIVE_SPREAD_DEGREES;
            ClassTargeting.Hit hit = ClassTargeting.traceMob(player, BOW_RANGE, yaw);
            ClassTargeting.trail(player.getInstance(), ClassTargeting.eye(player), hit.point(), Particle.CRIT);
            explode(player, hit.point(), DungeonClassScaling.EXPLOSIVE_SHOT_RADIUS, damage);
        }
    }

    private void rapidFire(SkyBlockPlayer player, int shotsLeft) {
        if (stopped || shotsLeft <= 0 || !player.isOnline() || classes.host().isGhost(player.getUuid())) return;
        double highest = classes.bowHits().highest(player.getUuid(), now());
        ClassTargeting.Hit hit = ClassTargeting.traceMob(player, BOW_RANGE, 0);
        ClassTargeting.trail(player.getInstance(), ClassTargeting.eye(player), hit.point(), Particle.CRIT);
        if (highest > 0 && hit.entity() instanceof SkyBlockMob mob) {
            AttackService.applyHit(player, mob, (float) (highest * DungeonClassScaling.RAPID_FIRE_DAMAGE_SHARE), false);
        }
        classes.host().later(RAPID_FIRE_INTERVAL_TICKS, () -> rapidFire(player, shotsLeft - 1));
    }

    private void seismicWave(SkyBlockPlayer player) {
        UUID uuid = player.getUuid();
        double defense = player.getStatistics().allStatistics().getOverall(ItemStatistic.DEFENSE);
        double damage = DungeonClassScaling.seismicWaveDamage(classes.level(uuid), defense, classes.strength(uuid));
        Pos origin = player.getPosition();
        Vec direction = new Vec(-Math.sin(Math.toRadians(origin.yaw())), 0, Math.cos(Math.toRadians(origin.yaw()))).normalize();
        Instance instance = player.getInstance();
        for (double step = 1; step <= SEISMIC_LENGTH; step++) {
            ClassTargeting.particle(instance, origin.add(direction.mul(step)), Particle.EXPLOSION, 1);
        }
        for (SkyBlockMob mob : ClassTargeting.mobsNear(instance, origin, SEISMIC_LENGTH + SEISMIC_WIDTH)) {
            Vec offset = Vec.fromPoint(mob.getPosition().sub(origin)).withY(0);
            double along = offset.dot(direction);
            if (along < 0 || along > SEISMIC_LENGTH) continue;
            if (offset.sub(direction.mul(along)).length() > SEISMIC_WIDTH) continue;
            AttackService.applyHit(player, mob, (float) damage, false);
        }
    }

    private void castleOfStone(SkyBlockPlayer player) {
        classes.castle(player.getUuid(), now() + DungeonClassScaling.CASTLE_OF_STONE_SECONDS * 1000L);
        for (SkyBlockMob mob : ClassTargeting.mobsNear(player.getInstance(), player.getPosition(), DungeonClassScaling.CASTLE_OF_STONE_AGGRO_RADIUS)) {
            mob.setTarget(player);
        }
    }

    private void instantWall(SkyBlockPlayer player) {
        Instance instance = player.getInstance();
        Point hit = ClassTargeting.trace(player, TARGET_RANGE, 0, _ -> false).point();
        Pos origin = player.getPosition();
        Vec side = new Vec(Math.cos(Math.toRadians(origin.yaw())), 0, Math.sin(Math.toRadians(origin.yaw()))).normalize();
        List<BlockVec> placed = new ArrayList<>();
        for (int across = -2; across <= 2; across++) {
            for (int up = 0; up < 3; up++) {
                BlockVec block = new BlockVec(hit.add(side.mul(across)).add(0, up, 0));
                if (!instance.getBlock(block).isAir()) continue;
                instance.setBlock(block, Block.STONE_BRICKS);
                placed.add(block);
            }
        }
        classes.host().later(DungeonClassScaling.INSTANT_WALL_SECONDS * 20, () -> placed.forEach(block -> {
            if (instance.getBlock(block).compare(Block.STONE_BRICKS)) instance.setBlock(block, Block.AIR);
        }));
    }

    private void fireball(SkyBlockPlayer player, double damage) {
        ClassTargeting.Hit hit = ClassTargeting.traceMob(player, BOW_RANGE, 0);
        ClassTargeting.trail(player.getInstance(), ClassTargeting.eye(player), hit.point(), Particle.FLAME);
        explode(player, hit.point(), FIREBALL_RADIUS, damage);
    }

    private void stunBow(SkyBlockPlayer player) {
        ClassTargeting.Hit hit = ClassTargeting.traceMob(player, BOW_RANGE, 0);
        ClassTargeting.trail(player.getInstance(), ClassTargeting.eye(player), hit.point(), Particle.CRIT);
        if (hit.entity() instanceof SkyBlockMob mob && stunnable(mob)) {
            MobStatusEffects.stun(mob, DungeonClassScaling.STUN_SECONDS * 1000L);
        }
    }

    private void healingBow(SkyBlockPlayer player) {
        ClassTargeting.Hit hit = ClassTargeting.trace(player, BOW_RANGE, 0,
                entity -> entity instanceof SkyBlockPlayer mate && classes.host().alivePlayers().contains(mate));
        ClassTargeting.trail(player.getInstance(), ClassTargeting.eye(player), hit.point(), Particle.HEART);
        if (!(hit.entity() instanceof SkyBlockPlayer mate)) return;
        heal(mate, mate.getMaxHealth() * DungeonClassScaling.HEALING_BOW_PERCENT / 100.0, null);
        buff(mate, "Healing Bow", ItemStatistic.STRENGTH, DungeonClassScaling.HEALING_BOW_STRENGTH, DungeonClassScaling.HEALING_BOW_SECONDS);
    }

    private static boolean stunnable(SkyBlockMob mob) {
        return !(mob instanceof CatacombsMob dungeonMob) || dungeonMob.definition().role() != DungeonMobRole.BOSS;
    }

    private void explode(SkyBlockPlayer player, Point center, double radius, double damage) {
        ClassTargeting.particle(player.getInstance(), center, Particle.EXPLOSION, 3);
        for (SkyBlockMob mob : ClassTargeting.mobsNear(player.getInstance(), center, radius)) {
            AttackService.applyHit(player, mob, (float) damage, false);
        }
    }

    private List<SkyBlockPlayer> teammatesNear(SkyBlockPlayer ghost, double radius) {
        return classes.host().alivePlayers().stream()
                .filter(mate -> mate.getPosition().distance(ghost.getPosition()) <= radius)
                .toList();
    }

    private static void buff(SkyBlockPlayer player, String name, ItemStatistic stat, double amount, int seconds) {
        player.getStatistics().removeTemporaryStatistics(statistic -> name.equals(statistic.getDisplayName()));
        player.getStatistics().boostStatistic(TemporaryStatistic.builder()
                .withStatistics(ItemStatistics.builder().withBase(stat, amount).build())
                .withExpirationInMs(seconds * 1000L)
                .withDisplayName(name)
                .withDisplayColor("<c>")
                .build());
    }

    private void absorption(SkyBlockPlayer player, float amount, int seconds) {
        player.setAdditionalHearts(player.getAdditionalHearts() + amount);
        classes.host().later(seconds * 20, () -> player.setAdditionalHearts(Math.max(0, player.getAdditionalHearts() - amount)));
    }

    /** Heals a player; healing from a Healer gets Renew, and any part over full health becomes an Overheal shield. */
    void heal(SkyBlockPlayer player, double amount, SkyBlockPlayer healer) {
        if (amount <= 0 || player.isDead()) return;
        if (healer != null) amount *= 1 + DungeonClassScaling.renewPercent(classes.level(healer.getUuid()), classes.strength(healer.getUuid())) / 100;
        float max = player.getMaxHealth();
        double excess = player.getHealth() + amount - max;
        player.setHealth((float) Math.min(max, player.getHealth() + amount));
        if (healer == null || excess <= 0) return;
        double cap = max * (OVERHEAL_BASE_PERCENT + OVERHEAL_PER_LEVEL * classes.level(healer.getUuid())) / 100 * classes.strength(healer.getUuid());
        float shield = (float) Math.min(cap, player.getAdditionalHearts() + excess);
        if (shield > player.getAdditionalHearts()) player.setAdditionalHearts(shield);
    }

    void tickSecond() {
        if (stopped || !classes.host().classesActive()) return;
        long now = now();
        List<SkyBlockPlayer> alive = classes.host().alivePlayers();
        for (SkyBlockPlayer healer : alive) {
            if (classes.classOf(healer.getUuid()) != DungeonClassType.HEALER || !classes.inParty(healer.getUuid())) continue;
            double percent = DungeonClassScaling.healingAuraPercent(classes.level(healer.getUuid()), classes.strength(healer.getUuid()));
            for (SkyBlockPlayer mate : alive) {
                if (mate.getPosition().distance(healer.getPosition()) <= DungeonClassScaling.HEALING_AURA_RADIUS) {
                    heal(mate, mate.getMaxHealth() * percent / 100, healer);
                }
            }
        }
        circles.removeIf(circle -> circle.until() <= now);
        Instance instance = classes.host().instance();
        for (HealingCircle circle : circles) {
            ClassTargeting.ring(instance, circle.center(), circle.radius(), Particle.HAPPY_VILLAGER);
            SkyBlockPlayer healer = alive.stream().filter(each -> each.getUuid().equals(circle.healer())).findFirst().orElse(null);
            for (SkyBlockPlayer mate : alive) {
                if (mate.getPosition().distance(circle.center()) <= circle.radius()) {
                    heal(mate, mate.getMaxHealth() * circle.percent() / 100, healer);
                }
            }
        }
        storms.removeIf(storm -> storm.until() <= now);
        for (Thunderstorm storm : storms) strike(storm, alive);
    }

    private void strike(Thunderstorm storm, List<SkyBlockPlayer> alive) {
        SkyBlockPlayer mage = classes.host().onlineMembers().stream()
                .filter(each -> each.getUuid().equals(storm.mage())).findFirst().orElse(null);
        if (mage == null || !alive.contains(mage)) return;
        double damage = DungeonClassScaling.thunderstormDamage(classes.level(mage.getUuid()), classes.strength(mage.getUuid()));
        List<SkyBlockMob> targets = new ArrayList<>(ClassTargeting.mobsNear(mage.getInstance(), mage.getPosition(), DungeonClassScaling.THUNDERSTORM_RADIUS));
        targets.sort(Comparator.comparingDouble(_ -> ThreadLocalRandom.current().nextDouble()));
        for (SkyBlockMob mob : targets.subList(0, Math.min(THUNDERSTORM_STRIKES_PER_SECOND, targets.size()))) {
            dev.daweapon.type.skyblockgeneric.mythological.ability.MythologicalCombat.lightning(mob.getInstance(), mob.getPosition());
            AttackService.applyHit(mage, mob, (float) damage, false);
        }
    }

    void stop() {
        stopped = true;
        circles.clear();
        storms.clear();
    }

    private static long now() {
        return System.currentTimeMillis();
    }
}
