package net.swofty.type.kuudra.mob;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.metadata.cube.MagmaCubeMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.particle.Particle;
import net.swofty.type.kuudra.fight.KuudraFight;
import net.swofty.type.kuudra.fight.world.Displays;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraMobSpec;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.mythological.ability.MobProjectile;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/** The minions with their own tricks (research K1 5.3). */
public final class KuudraSpecialists {
    private KuudraSpecialists() {
    }

    /**
     * Emerge from Kuudra's mouth in a group, scatter and throw fireballs. Each fireball deals the tier's base
     * damage times how often this blaze has already hit that player in the window, ignoring defence.
     */
    public static final class WanderingBlaze extends KuudraMob {
        private final Map<UUID, List<Long>> hits = new ConcurrentHashMap<>();
        private Pos hover;

        public WanderingBlaze(KuudraFight fight, KuudraMobSpec spec) {
            super(fight, spec, EntityType.BLAZE, MobType.INFERNAL);
            setNoGravity(true);
        }

        @Override
        protected int strollRadius() {
            return 0;
        }

        /** Blazes leave Kuudra's mouth and settle hovering over the platform (the Burning capture). */
        private void drift() {
            if (hover == null) hover = fight.randomPlatformPoint(6).add(0, 7, 0);
            Vec toward = hover.sub(getPosition()).asVec();
            if (toward.length() > 0.5) teleport(getPosition().add(toward.normalize().mul(Math.min(0.4, toward.length()))));
        }

        @Override
        protected boolean melee() {
            return false;
        }

        public static double fireballDamage(int tierDps, int hitsInWindow, double multiplier) {
            return tierDps * Math.max(1, hitsInWindow) * multiplier;
        }

        @Override
        protected void behave(int ticks) {
            drift();
            if (ticks % (fight.spec().mobs().blazeFireSeconds() * 20) != 0) return;
            nearestTarget(28).ifPresent(target -> MobProjectile.item(this, target, ItemStack.of(Material.FIRE_CHARGE), 0.9, hit -> {
                long now = System.currentTimeMillis();
                long window = fight.spec().mobs().blazeHitWindowSeconds() * 1000L;
                List<Long> recent = hits.computeIfAbsent(hit.getUuid(), _ -> new ArrayList<>());
                recent.removeIf(time -> now - time > window);
                recent.add(now);
                trueStrike(hit, fireballDamage(fight.tierSpec().wanderingBlazeDps(), recent.size(),
                        fight.spec().mobs().blazeHitMultiplier()));
            }));
        }
    }

    /**
     * Flies a fixed loop high above the platform edge; after most of the loop it heads to the middle and drops TNT
     * that hits every player. Shot down first, it drops nothing.
     */
    public static final class Dropship extends KuudraMob {
        private int flightTicks;
        private boolean bombing;
        private final double startAngle = ThreadLocalRandom.current().nextDouble(Math.PI * 2);

        public Dropship(KuudraFight fight, KuudraMobSpec spec) {
            super(fight, spec, EntityType.GHAST, MobType.CUBIC, MobType.ARCANE, MobType.AIRBORNE);
            setNoGravity(true);
        }

        @Override
        public List<GoalSelector> getGoalSelectors() {
            return List.of();
        }

        @Override
        public List<TargetSelector> getTargetSelectors() {
            return List.of();
        }

        /** Where the ship is on its loop after {@code ticks}, and whether it has turned for the middle. */
        public static Pos loopPosition(Pos centre, double radius, double altitude, double startAngle, int ticks, int loopTicks,
                                       double attackFraction) {
            int turn = (int) (loopTicks * attackFraction);
            if (ticks <= turn) {
                double angle = startAngle + Math.PI * 2 * ticks / loopTicks;
                return centre.add(Math.cos(angle) * radius, altitude, Math.sin(angle) * radius);
            }
            double angle = startAngle + Math.PI * 2 * turn / loopTicks;
            Pos edge = centre.add(Math.cos(angle) * radius, altitude, Math.sin(angle) * radius);
            double toCentre = Math.min(1, (ticks - turn) / 100.0);
            Pos middle = centre.add(0, altitude, 0);
            return edge.add(middle.sub(edge).mul(toCentre));
        }

        @Override
        protected void behave(int ticks) {
            if (bombing) return;
            flightTicks++;
            var mobs = fight.spec().mobs();
            int loopTicks = mobs.dropshipLoopSeconds() * 20;
            Pos at = loopPosition(fight.centre(), fight.loopRadius(), mobs.dropshipAltitude(), startAngle, flightTicks,
                    loopTicks, mobs.dropshipAttackFraction());
            teleport(at.withDirection(at.sub(getPosition())));
            if (flightTicks >= (int) (loopTicks * mobs.dropshipAttackFraction()) + 100) {
                bombing = true;
                fight.dropshipBomb(this, damageStat());
                remove();
            }
        }
    }

    /** Leaps slowly around the platform, leaving a ring of particles where it lands, and sometimes teleports. */
    public static final class Slasher extends KuudraMob {
        public Slasher(KuudraFight fight, KuudraMobSpec spec) {
            super(fight, spec, EntityType.MAGMA_CUBE, MobType.INFERNAL);
            editEntityMeta(net.minestom.server.entity.metadata.cube.MagmaCubeMeta.class, meta -> meta.setSize(4));
        }

        @Override
        protected void behave(int ticks) {
            int every = fight.spec().mobs().slasherLeapSeconds() * 20;
            if (ticks % every == every / 2 && isOnGround()) fight.ring(getPosition(), 2.5, Particle.CRIT);
            if (ticks % every != 0) return;
            if (ThreadLocalRandom.current().nextDouble() < fight.spec().mobs().slasherTeleportChance()) {
                teleport(fight.randomPlatformPoint(4));
                return;
            }
            Pos goal = nearestTarget(40).map(Entity::getPosition).orElseGet(() -> fight.randomPlatformPoint(4));
            Vec leap = goal.sub(getPosition()).asVec().withY(0);
            if (leap.lengthSquared() > 1e-4) setVelocity(leap.normalize().mul(Math.min(14, leap.length() * 1.5)).withY(9));
        }
    }

    /** Stands still, orbited by Wither skulls that burn on contact; with too many skulls, it throws them. */
    public static final class WitherSentry extends KuudraMob {
        private final List<Entity> skulls = new ArrayList<>();

        public WitherSentry(KuudraFight fight, KuudraMobSpec spec) {
            super(fight, spec, EntityType.WITHER, MobType.WITHER, MobType.CONSTRUCT);
            getAttribute(Attribute.SCALE).setBaseValue(0.6);
        }

        @Override
        protected boolean melee() {
            return false;
        }

        @Override
        public boolean isImmuneToArrows() {
            return true;
        }

        @Override
        protected void behave(int ticks) {
            var mobs = fight.spec().mobs();
            if (ticks % (mobs.sentrySkullSeconds() * 20) == 0) {
                if (skulls.size() < mobs.sentryMaxSkulls()) {
                    skulls.add(Displays.item(getInstance(), getPosition(), ItemStack.of(Material.WITHER_SKELETON_SKULL), 0.6f));
                } else {
                    nearestTarget(24).ifPresent(target -> {
                        Displays.remove(skulls.removeLast());
                        MobProjectile.item(this, target, ItemStack.of(Material.WITHER_SKELETON_SKULL), 0.6,
                                hit -> strike(hit, damageStat()));
                    });
                }
            }
            for (int i = 0; i < skulls.size(); i++) {
                double angle = ticks * 0.12 + Math.PI * 2 * i / skulls.size();
                Pos at = getPosition().add(Math.cos(angle) * 2, 1.2, Math.sin(angle) * 2);
                skulls.get(i).teleport(at);
                if (ticks % 10 == 0) {
                    for (SkyBlockPlayer player : fight.alivePlayers()) {
                        if (player.getPosition().add(0, 1, 0).distance(at) < 1.1) strike(player, damageStat());
                    }
                }
            }
        }

        @Override
        public void remove() {
            skulls.forEach(Displays::remove);
            skulls.clear();
            super.remove();
        }
    }

    /** Throws exploding iron blocks and leaps at players, slamming down in a ring. */
    public static final class BlazingGolem extends KuudraMob {
        private int landingAt = -1;

        public BlazingGolem(KuudraFight fight, KuudraMobSpec spec) {
            super(fight, spec, EntityType.IRON_GOLEM, MobType.WITHER, MobType.CONSTRUCT);
        }

        @Override
        public boolean attacksBallista() {
            return true;
        }

        @Override
        protected void behave(int ticks) {
            var mobs = fight.spec().mobs();
            if (ticks % (mobs.golemThrowSeconds() * 20) == 0) {
                nearestTarget(24).ifPresent(target -> MobProjectile.item(this, target, ItemStack.of(Material.IRON_BLOCK), 0.8,
                        hit -> {
                            fight.ring(hit.getPosition(), 1.5, Particle.EXPLOSION);
                            strike(hit, damageStat() * mobs.golemThrowMultiplier());
                        }));
            }
            if (ticks % (mobs.golemLeapSeconds() * 20) == 0) {
                nearestTarget(20).ifPresent(target -> {
                    Vec leap = target.getPosition().sub(getPosition()).asVec().withY(0);
                    if (leap.lengthSquared() > 1e-4) setVelocity(leap.normalize().mul(Math.min(16, leap.length() * 1.6)).withY(10));
                    landingAt = ticks + 20;
                });
            }
            if (ticks == landingAt) {
                fight.ring(getPosition(), 3, Particle.FLAME);
                for (SkyBlockPlayer player : fight.alivePlayers()) {
                    if (player.getPosition().distance(getPosition()) <= 3) strike(player, damageStat() * mobs.golemLeapMultiplier());
                }
            }
        }
    }

    /** Magma Followers try to meet; when two of a group touch, the group becomes an Omegagma. */
    public static final class MagmaFollower extends KuudraMob {
        private final List<MagmaFollower> group;

        public MagmaFollower(KuudraFight fight, KuudraMobSpec spec, List<MagmaFollower> group) {
            super(fight, spec, EntityType.MAGMA_CUBE, MobType.CUBIC, MobType.MAGMATIC);
            this.group = group;
            editEntityMeta(MagmaCubeMeta.class, meta -> meta.setSize(2));
        }

        @Override
        protected Temper temper() {
            return Temper.PASSIVE;
        }

        @Override
        protected int strollRadius() {
            return 0;
        }

        @Override
        protected void behave(int ticks) {
            List<MagmaFollower> alive = group.stream().filter(each -> !each.isDead() && !each.isRemoved()).toList();
            if (alive.size() < 2) return;
            Pos centre = centroid(alive);
            for (MagmaFollower other : alive) {
                if (other != this && other.getPosition().distance(getPosition()) <= fight.spec().mobs().magmaMergeRange()) {
                    fight.mergeMagmaFollowers(alive, centre);
                    return;
                }
            }
            Vec toward = centre.sub(getPosition()).asVec().withY(0);
            if (ticks % 10 == 0 && isOnGround() && toward.lengthSquared() > 0.04) {
                setVelocity(toward.normalize().mul(5).withY(5));
            }
        }

        public static Pos centroid(List<? extends Entity> members) {
            double x = 0, y = 0, z = 0;
            for (Entity member : members) {
                x += member.getPosition().x();
                y += member.getPosition().y();
                z += member.getPosition().z();
            }
            return new Pos(x / members.size(), y / members.size(), z / members.size());
        }
    }

    /** Sits still for a few seconds, then explodes, hurting everyone looking at it. */
    public static final class Omegagma extends KuudraMob {
        public Omegagma(KuudraFight fight, KuudraMobSpec spec) {
            super(fight, spec, EntityType.MAGMA_CUBE, MobType.CUBIC, MobType.MAGMATIC);
            editEntityMeta(MagmaCubeMeta.class, meta -> meta.setSize(6));
        }

        @Override
        protected Temper temper() {
            return Temper.PASSIVE;
        }

        @Override
        protected int strollRadius() {
            return 0;
        }

        @Override
        protected String nameHeader() {
            return "<c><l>LOOK AWAY!";
        }

        @Override
        protected void behave(int ticks) {
            if (ticks < fight.spec().mobs().omegagmaFuseSeconds() * 20) return;
            fight.ring(getPosition(), 4, Particle.EXPLOSION_EMITTER);
            for (SkyBlockPlayer player : fight.alivePlayers()) {
                if (isLookingAt(player, getPosition().add(0, 1.5, 0), fight.spec().mobs().omegagmaLookDot())) {
                    strike(player, damageStat());
                }
            }
            remove();
        }

        public static boolean isLookingAt(SkyBlockPlayer player, Pos target, double minDot) {
            Pos eye = player.getPosition().add(0, player.getEyeHeight(), 0);
            Vec toTarget = target.sub(eye).asVec();
            if (toTarget.length() > 48 || toTarget.lengthSquared() < 1e-4) return false;
            return player.getPosition().direction().normalize().dot(toTarget.normalize()) >= minDot;
        }
    }
}
