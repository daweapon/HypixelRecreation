package dev.daweapon.type.theend.dragon;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobProfile;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.mythological.ability.MobProjectile;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public final class EnderDragonMob extends CrimsonIsleMob {
    private static final int BASE_ATTACK_INTERVAL = 110;
    private static final int CONTACT_INTERVAL = 40;
    private static final int LIGHTNING_DELAY = 20;
    private static final int HOVER_TICKS = 200;
    private static final int HOVER_FIREBALL_INTERVAL = 20;
    private static final double RUSH_DAMAGE = 2000;
    private static final double RUSH_TRUE = 550;
    private static final double BOLT_DAMAGE = 1700;
    private static final double BOLT_TRUE = 200;
    private static final double CONTACT_PERCENT = 0.5;
    private static final double BERSERK_THRESHOLD = 0.5;
    private static final double BERSERK_MULTIPLIER = 1.5;
    private static final double CRYSTAL_HEAL_PERCENT_PER_SECOND = 0.003;
    private static final double CRYSTAL_RANGE = 32;
    private static final double CONTACT_RANGE = 5;
    private static final double RUSH_RANGE = 5;
    private static final double UNSTABLE_EXPLOSION = 2000;
    private static final double UNSTABLE_CONTACT = 3000;
    private static final double UNSTABLE_EXPLOSION_RADIUS = 12;

    private final DragonVariant variant;
    private final Consumer<EnderDragonMob> onDefeated;
    private final java.util.function.ToIntBiFunction<Pos, Double> crystalsNear;
    private Pos orbitTarget = DragonArena.CENTER;
    private Pos rushTarget;
    private int rushTicks;
    private int hoverTicks;
    private double angle = ThreadLocalRandom.current().nextDouble(Math.PI * 2);

    public EnderDragonMob(DragonVariant variant, java.util.function.ToIntBiFunction<Pos, Double> crystalsNear, Consumer<EnderDragonMob> onDefeated) {
        super(profile(variant));
        this.variant = variant;
        this.crystalsNear = crystalsNear;
        setCustomNameVisible(false);
        this.onDefeated = onDefeated;
        setNoGravity(true);
        hasPhysics = false;
    }

    private static CrimsonMobProfile profile(DragonVariant variant) {
        return CrimsonMobProfile.builder("ENDER_DRAGON_" + variant.name(), variant.displayName())
                .level(variant.level())
                .entity(EntityType.ENDER_DRAGON)
                .stats(variant.health(), variant.damage())
                .defense(variant.defense())
                .speed(variant.speed())
                .rewards(variant.combatXp(), variant.coins(), variant.orbs())
                .types(MobType.ENDER, MobType.AIRBORNE)
                .neverCorrupted()
                .build();
    }

    public DragonVariant variant() {
        return variant;
    }

    @Override public boolean countsForBestiary() { return false; }
    @Override public boolean countsTowardVanquisher() { return false; }
    @Override public boolean hasCollision() { return false; }
    @Override public float getNameDisplayHeightOffset() { return 5f; }
    @Override public List<GoalSelector> getGoalSelectors() { return List.of(); }
    @Override public List<TargetSelector> getTargetSelectors() { return List.of(); }

    private boolean berserk() {
        return getHealth() < getMaxHealth() * BERSERK_THRESHOLD;
    }

    private double berserkFactor() {
        return berserk() ? BERSERK_MULTIPLIER : 1;
    }

    private double blocksPerTick() {
        return 0.45 * variant.speed() / 140;
    }

    @Override
    protected void movementTick() {
        if (hoverTicks > 0) {
            setVelocity(Vec.ZERO);
            return;
        }
        Pos current = getPosition();
        Pos goal = rushTarget != null ? rushTarget : orbitTarget;
        Vec toGoal = goal.sub(current).asVec();
        double distance = toGoal.length();
        if (rushTarget == null && distance < 4) {
            pickOrbitTarget();
            return;
        }
        double speed = blocksPerTick() * (rushTarget != null ? 2.2 : 1);
        Vec velocity = distance < 0.001 ? Vec.ZERO : toGoal.normalize().mul(speed * 20);
        setVelocity(velocity);
        if (distance > 0.5) lookAt(current.add(-velocity.x(), velocity.y(), -velocity.z()));
        super.movementTick();
    }

    private void pickOrbitTarget() {
        angle += ThreadLocalRandom.current().nextDouble(0.4, 0.9);
        double radius = DragonArena.ORBIT_RADIUS * (0.5 + ThreadLocalRandom.current().nextDouble(0.5));
        double height = DragonArena.flightHeight(variant) + ThreadLocalRandom.current().nextDouble(-6, 6);
        orbitTarget = new Pos(DragonArena.CENTER.x() + Math.cos(angle) * radius,
                Math.min(DragonArena.CEILING, Math.max(18, height)),
                DragonArena.CENTER.z() + Math.sin(angle) * radius);
    }

    @Override
    protected void onAiTick(int ticks) {
        if (ticks % 20 == 0) healFromCrystals();
        if (hoverTicks > 0) {
            hover();
            return;
        }
        if (rushTarget != null) {
            continueRush();
            return;
        }
        if (ticks % CONTACT_INTERVAL == 0) contactStrike();
        int interval = (int) (BASE_ATTACK_INTERVAL / (berserk() ? 1.3 : 1));
        if (ticks % interval == 0) attack();
    }

    private void healFromCrystals() {
        int live = crystalsNear.applyAsInt(getPosition(), CRYSTAL_RANGE);
        if (live <= 0 || getHealth() >= getMaxHealth()) return;
        setHealth((float) Math.min(getMaxHealth(), getHealth() + getMaxHealth() * CRYSTAL_HEAL_PERCENT_PER_SECOND * live));
        refreshName();
    }

    private Optional<SkyBlockPlayer> target() {
        return nearestPlayer(80, player -> true);
    }

    private void attack() {
        Optional<SkyBlockPlayer> target = target();
        if (target.isEmpty()) return;
        int choice = ThreadLocalRandom.current().nextInt(variant == DragonVariant.WISE ? 5 : 3);
        switch (choice) {
            case 0 -> startRush(target.get());
            case 1 -> lightning(target.get());
            default -> hoverTicks = HOVER_TICKS;
        }
    }

    private void hover() {
        hoverTicks--;
        if (hoverTicks % HOVER_FIREBALL_INTERVAL == 0) target().ifPresent(this::fireball);
    }

    private void startRush(SkyBlockPlayer target) {
        rushTarget = target.getPosition().add(0, 1, 0);
        rushTicks = 0;
    }

    private void continueRush() {
        rushTicks++;
        Pos at = getPosition();
        for (SkyBlockPlayer player : playersWithin(RUSH_RANGE)) {
            strike(player, RUSH_DAMAGE * berserkFactor() * variant.rushMultiplier());
            trueStrike(player, RUSH_TRUE * berserkFactor() * variant.rushMultiplier());
            rushTarget = null;
            pickOrbitTarget();
            return;
        }
        if (at.distance(rushTarget) < 3 || rushTicks > 60) {
            rushTarget = null;
            pickOrbitTarget();
        }
    }

    private void fireball(SkyBlockPlayer target) {
        double multiplier = berserkFactor() * variant.projectileMultiplier();
        MobProjectile.item(this, target, ItemStack.of(Material.FIRE_CHARGE), 1.0, hit -> {
            strike(hit, BOLT_DAMAGE * multiplier);
            trueStrike(hit, BOLT_TRUE * multiplier);
            hit.setVelocity(hit.getVelocity().add(0, 14, 0));
        });
    }

    private void lightning(SkyBlockPlayer target) {
        Pos strikeAt = target.getPosition();
        Entity bolt = new Entity(EntityType.LIGHTNING_BOLT);
        bolt.setInstance(getInstance(), strikeAt);
        double multiplier = berserkFactor() * variant.projectileMultiplier();
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (!bolt.isRemoved()) bolt.remove();
            if (isRemoved() || isDead() || getInstance() == null) return;
            for (SkyBlockPlayer player : playersNear(strikeAt, 2.5)) {
                if (covered(player)) continue;
                strike(player, BOLT_DAMAGE * multiplier);
                trueStrike(player, BOLT_TRUE * multiplier);
            }
        }).delay(TaskSchedule.tick(LIGHTNING_DELAY)).schedule();
    }

    private List<SkyBlockPlayer> playersNear(Pos center, double range) {
        return getInstance().getPlayers().stream()
                .filter(player -> player instanceof SkyBlockPlayer && !player.isDead())
                .map(player -> (SkyBlockPlayer) player)
                .filter(player -> player.getPosition().distance(center) <= range)
                .toList();
    }

    private boolean covered(SkyBlockPlayer player) {
        Pos at = player.getPosition();
        for (int dy = 2; dy <= 8; dy++) {
            if (getInstance().getBlock(at.add(0, dy, 0)).isSolid()) return true;
        }
        return false;
    }

    private void contactStrike() {
        double flat = variant == DragonVariant.UNSTABLE ? UNSTABLE_CONTACT : 2000;
        for (SkyBlockPlayer player : playersWithin(CONTACT_RANGE)) {
            double damage = player.getMaxHealth() * CONTACT_PERCENT + flat;
            strike(player, damage * berserkFactor() * variant.contactMultiplier());
        }
    }

    @Override
    protected void onKilledBy(SkyBlockPlayer killer) {
        if (variant == DragonVariant.UNSTABLE) explode();
        onDefeated.accept(this);
    }

    private void explode() {
        for (SkyBlockPlayer player : playersWithin(UNSTABLE_EXPLOSION_RADIUS)) {
            trueStrike(player, UNSTABLE_EXPLOSION);
        }
    }

    public void abandon() {
        if (!isRemoved()) remove();
    }
}
