package dev.daweapon.type.skyblockgeneric.hunting.creature;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.hunting.creature.special.CreatureSpecial;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Wiki Lasso: throw it at a forest creature, its stamina bar drains, and once the bar is low a right-click reels the
 * creature in; a creature has one stamina bar per shard rarity level (1 to 5) and needs one reel per bar. Reeling too
 * early, looking away or reeling too late lets go. Several players lassoing one creature empty its bar at once, and
 * higher lassos drain faster (video: second tier +20%, Everstretch +60%). Throw ranges, the base 12%/s drain and the
 * reel window are estimates.
 */
public final class LassoService {
    public static final double REEL_THRESHOLD = 20;
    public static final double DRAIN_PER_SECOND = 12;
    private static final long REEL_WINDOW_MS = 2500;
    private static final long LOOK_AWAY_LIMIT_MS = 1200;
    private static final double MAX_LOOK_ANGLE_DEGREES = 55;
    private static final double AIM_COSINE = Math.cos(Math.toRadians(14));
    private static final int TICK_STEP = 2;

    public enum Tier {
        ABYSMAL(ItemType.ABYSMAL_LASSO, 8, 1.0), VINERIP(ItemType.VINERIP_LASSO, 10, 1.2),
        ENTANGLER(ItemType.ENTANGLER_LASSO, 12, 1.4), EVERSTRETCH(ItemType.EVERSTRETCH_LASSO, 14, 1.6);

        private final ItemType item;
        private final double range;
        private final double drain;

        Tier(ItemType item, double range, double drain) {
            this.item = item;
            this.range = range;
            this.drain = drain;
        }

        public double drain() {
            return drain;
        }

        public ItemType item() {
            return item;
        }

        public double range() {
            return range;
        }
    }

    private static final class Session {
        private final HuntingCreatureMob mob;
        private final Tier tier;
        private int barsLeft;
        private double stamina = 100;
        private long reelDeadline;
        private long lookAway;

        private Session(HuntingCreatureMob mob, Tier tier) {
            this.mob = mob;
            this.tier = tier;
            this.barsLeft = barsFor(mob.spec());
        }
    }

    static int barsFor(CreatureSpec spec) {
        return CreatureCapture.shardOf(spec).map(shard -> shard.rarity().ordinal() + 1).orElse(Math.max(1, spec.staminaBars()));
    }

    private static final Map<UUID, Session> SESSIONS = new ConcurrentHashMap<>();
    private static boolean ticking;

    private LassoService() {
    }

    public static @Nullable Tier tierOf(ItemType type) {
        for (Tier tier : Tier.values()) if (tier.item == type) return tier;
        return null;
    }

    public static boolean attached(SkyBlockPlayer player) {
        return SESSIONS.containsKey(player.getUuid());
    }

    public static Optional<HuntingCreatureMob> attachedTo(SkyBlockPlayer player) {
        Session session = SESSIONS.get(player.getUuid());
        return session == null ? Optional.empty() : Optional.of(session.mob);
    }

    public static void release(SkyBlockPlayer player) {
        Session session = SESSIONS.get(player.getUuid());
        if (session != null) detach(player, session, true);
    }

    public static java.util.OptionalDouble stamina(HuntingCreatureMob mob) {
        return SESSIONS.values().stream().filter(session -> session.mob == mob).mapToDouble(session -> session.stamina).findFirst();
    }

    public static int barsLeft(HuntingCreatureMob mob) {
        return SESSIONS.values().stream().filter(session -> session.mob == mob).mapToInt(session -> session.barsLeft).findFirst().orElse(0);
    }

    public static boolean isLassoed(HuntingCreatureMob mob) {
        return SESSIONS.values().stream().anyMatch(session -> session.mob == mob);
    }

    public static void use(SkyBlockPlayer player, Tier tier) {
        Session session = SESSIONS.get(player.getUuid());
        if (session != null) {
            reel(player, session);
            return;
        }
        Optional<HuntingCreatureMob> target = target(player, tier.range());
        if (target.isEmpty()) {
            player.sendMessage("<c>Aim at a forest creature to throw the Lasso!");
            return;
        }
        HuntingCreatureMob mob = target.get();
        if (!mob.spec().captures(CreatureSpec.Capture.LASSO) && !mob.special().map(CreatureSpecial::lassoable).orElse(false)) {
            player.sendMessage("<c>You can't catch a <f>{}<c> with a Lasso!", mob.spec().name());
            return;
        }
        if (mob.special().map(special -> !special.allowLasso(player, mob)).orElse(false)) return;
        SESSIONS.put(player.getUuid(), new Session(mob, tier));
        if (SESSIONS.values().stream().filter(other -> other.mob == mob).count() > 1) {
            SESSIONS.values().stream().filter(other -> other.mob == mob).forEach(other -> other.stamina = 0);
        }
        player.sendMessage("<a>You threw your Lasso at the <f>{}<a>! <7>Reel it in when its stamina runs low.", mob.spec().name());
        startTicking();
    }

    private static Optional<HuntingCreatureMob> target(SkyBlockPlayer player, double range) {
        if (player.getInstance() == null) return Optional.empty();
        Vec look = player.getPosition().direction();
        return player.getInstance().getNearbyEntities(player.getPosition(), range).stream()
                .filter(entity -> entity instanceof HuntingCreatureMob mob && !mob.isDead())
                .map(entity -> (HuntingCreatureMob) entity)
                .filter(mob -> {
                    Vec toMob = Vec.fromPoint(mob.getPosition().add(0, 0.5, 0).sub(player.getPosition().add(0, player.getEyeHeight(), 0)));
                    return toMob.lengthSquared() > 0 && toMob.normalize().dot(look) >= AIM_COSINE;
                })
                .min(Comparator.comparingDouble(mob -> mob.getPosition().distanceSquared(player.getPosition())));
    }

    private static void reel(SkyBlockPlayer player, Session session) {
        if (session.stamina > REEL_THRESHOLD) {
            player.sendMessage("<c>You reeled in too early! The creature got away from the Lasso!");
            detach(player, session, true);
            return;
        }
        session.barsLeft--;
        if (session.barsLeft <= 0) {
            SESSIONS.remove(player.getUuid());
            session.mob.special().ifPresent(special -> special.onLassoDetach(player, session.mob));
            session.mob.setHeader("");
            if (session.mob.special().map(special -> special.onLassoReeled(player, session.mob)).orElse(false)) return;
            session.mob.capture(player);
            return;
        }
        session.stamina = 100;
        session.reelDeadline = 0;
        player.sendMessage("<a>Reeled in a stamina bar! <7>{} left.", session.barsLeft);
    }

    private static void detach(SkyBlockPlayer player, Session session, boolean refund) {
        SESSIONS.remove(player.getUuid());
        session.mob.special().ifPresent(special -> special.onLassoDetach(player, session.mob));
        if (!session.mob.isDead()) session.mob.setHeader("");
    }

    private static void startTicking() {
        if (ticking) return;
        ticking = true;
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            Iterator<Session> iterator = SESSIONS.values().iterator();
            while (iterator.hasNext()) {
                Session session = iterator.next();
                SkyBlockPlayer owner = ownerOf(session);
                if (owner == null || session.mob.isDead() || session.mob.isRemoved()) {
                    iterator.remove();
                    if (!session.mob.isDead() && !session.mob.isRemoved()) session.mob.setHeader("");
                    continue;
                }
                tick(owner, session);
            }
            return TaskSchedule.tick(TICK_STEP);
        });
    }

    private static @Nullable SkyBlockPlayer ownerOf(Session session) {
        for (Map.Entry<UUID, Session> entry : SESSIONS.entrySet()) {
            if (entry.getValue() == session) {
                return dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader.getLoadedPlayers().stream()
                        .filter(player -> player.getUuid().equals(entry.getKey()) && !player.isDead()).findFirst().orElse(null);
            }
        }
        return null;
    }

    private static void tick(SkyBlockPlayer player, Session session) {
        HuntingCreatureMob mob = session.mob;
        if (player.getInstance() != mob.getInstance()
                || player.getPosition().distance(mob.getPosition()) > session.tier.range() * 1.6) {
            player.sendMessage("<c>The Lasso snapped!");
            detach(player, session, true);
            return;
        }
        Vec toMob = Vec.fromPoint(mob.getPosition().sub(player.getPosition()));
        double angle = toMob.lengthSquared() == 0 ? 0 : Math.toDegrees(Math.acos(Math.max(-1, Math.min(1, toMob.normalize().dot(player.getPosition().direction())))));
        session.lookAway = angle > MAX_LOOK_ANGLE_DEGREES ? session.lookAway + TICK_STEP * 50L : 0;
        if (session.lookAway > LOOK_AWAY_LIMIT_MS) {
            player.sendMessage("<c>You looked away and the creature slipped out of the Lasso!");
            detach(player, session, true);
            return;
        }
        long ticks = mob.getAliveTicks();
        mob.special().ifPresent(special -> special.onLassoTick(player, mob, ticks));
        if (SESSIONS.get(player.getUuid()) != session) return;
        double drain = DRAIN_PER_SECOND * session.tier.drain() * (TICK_STEP * 50 / 1000D)
                * (1 + dev.daweapon.type.skyblockgeneric.hunting.AttributeEffectService.value(player.getHuntingData(),
                dev.daweapon.type.skyblockgeneric.hunting.AttributeId.parse("E9")) / 100D);
        session.stamina = Math.max(0, session.stamina - drain);
        long now = System.currentTimeMillis();
        if (session.stamina <= REEL_THRESHOLD && session.reelDeadline == 0) {
            session.reelDeadline = now + REEL_WINDOW_MS;
            player.showTitle(Title.title(Component.text(""), net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection()
                    .deserialize("§a§lREEL"), Title.Times.times(Duration.ZERO, Duration.ofMillis(900), Duration.ofMillis(100))));
        }
        if (session.reelDeadline != 0 && now > session.reelDeadline) {
            player.sendMessage("<c>You were too slow! The creature regained its stamina and escaped!");
            detach(player, session, true);
            return;
        }
        mob.setHeader(bar(session));
    }

    private static String bar(Session session) {
        int filled = (int) Math.round(session.stamina / 10D);
        return "<9>Stamina <8>[<a>" + "▮".repeat(filled) + "<7>" + "▯".repeat(10 - filled) + "<8>] <7>x" + session.barsLeft;
    }
}
