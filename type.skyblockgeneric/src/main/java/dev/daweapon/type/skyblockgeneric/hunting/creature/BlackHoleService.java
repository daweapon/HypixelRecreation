package dev.daweapon.type.skyblockgeneric.hunting.creature;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeDefinition;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeEffectService;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeId;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.TaskSchedule;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Wiki Pocket Black Hole: placed for 180 s, it pulls combat creatures at or below 10% health toward it and captures
 * their shard after 2 s per shard rarity level; a capture resets the timer. The 8 block pull radius and the pull
 * strength are estimates.
 */
public final class BlackHoleService {
    public static final long LIFETIME_MS = 180_000;
    public static final long COOLDOWN_MS = 2000;
    private static final double RADIUS = 8;
    private static final double CAPTURE_DISTANCE = 2.2;
    private static final double PULL_SPEED = 6;
    private static final int TICK_STEP = 5;

    public static final Map<ItemType, Double> HUNTER_FORTUNE = Map.of(
            ItemType.SMALL_POCKET_BLACK_HOLE, 0D, ItemType.MEDIUM_POCKET_BLACK_HOLE, 2.5, ItemType.LARGE_POCKET_BLACK_HOLE, 5D);

    private static final class Hole {
        private final Instance instance;
        private final Pos centre;
        private long expiresAt = System.currentTimeMillis() + LIFETIME_MS;
        private HuntingCreatureMob capturing;
        private double progress;

        private Hole(Instance instance, Pos centre) {
            this.instance = instance;
            this.centre = centre;
        }
    }

    private static final Map<UUID, Hole> HOLES = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> LAST_PLACE = new ConcurrentHashMap<>();
    private static boolean ticking;

    private BlackHoleService() {
    }

    public static boolean active(SkyBlockPlayer player) {
        return HOLES.containsKey(player.getUuid());
    }

    public static void place(SkyBlockPlayer player) {
        long now = System.currentTimeMillis();
        if (now - LAST_PLACE.getOrDefault(player.getUuid(), 0L) < COOLDOWN_MS) return;
        LAST_PLACE.put(player.getUuid(), now);
        if (player.getInstance() == null) return;
        var target = player.getTargetBlockPosition(6);
        Pos centre = target == null ? player.getPosition().add(0, 1, 0) : new Pos(target.x() + 0.5, target.y() + 1.5, target.z() + 0.5);
        HOLES.put(player.getUuid(), new Hole(player.getInstance(), centre));
        player.sendMessage("<5>You placed a Black Hole! <7>It pulls weak creatures in for <a>180s<7>.");
        startTicking();
    }

    private static void startTicking() {
        if (ticking) return;
        ticking = true;
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            Iterator<Map.Entry<UUID, Hole>> iterator = HOLES.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<UUID, Hole> entry = iterator.next();
                SkyBlockPlayer owner = SkyBlockGenericLoader.getLoadedPlayers().stream()
                        .filter(player -> player.getUuid().equals(entry.getKey())).findFirst().orElse(null);
                if (owner == null || System.currentTimeMillis() > entry.getValue().expiresAt) {
                    iterator.remove();
                    if (owner != null) owner.sendMessage("<7>Your Black Hole collapsed.");
                    continue;
                }
                tick(owner, entry.getValue());
            }
            return TaskSchedule.tick(TICK_STEP);
        });
    }

    private static void tick(SkyBlockPlayer owner, Hole hole) {
        hole.instance.sendGroupedPacket(new ParticlePacket(Particle.PORTAL, false, false, hole.centre.x(), hole.centre.y(), hole.centre.z(),
                0.4f, 0.4f, 0.4f, 0.5f, 12));
        if (hole.capturing != null && (hole.capturing.isDead() || hole.capturing.isRemoved())) {
            hole.capturing = null;
            hole.progress = 0;
        }
        List<HuntingCreatureMob> candidates = hole.instance.getNearbyEntities(hole.centre, RADIUS).stream()
                .filter(entity -> entity instanceof HuntingCreatureMob mob && !mob.isDead() && mob.atHuntFloor()
                        && mob.spec().captures(CreatureSpec.Capture.BLACK_HOLE))
                .map(entity -> (HuntingCreatureMob) entity).toList();
        for (HuntingCreatureMob mob : candidates) {
            Vec pull = Vec.fromPoint(hole.centre.sub(mob.getPosition()));
            if (pull.length() > CAPTURE_DISTANCE) mob.setVelocity(pull.normalize().mul(PULL_SPEED));
        }
        if (hole.capturing == null) {
            hole.capturing = candidates.stream().filter(mob -> mob.getPosition().distance(hole.centre) <= CAPTURE_DISTANCE).findFirst().orElse(null);
            hole.progress = 0;
            return;
        }
        hole.progress += TICK_STEP * 50 / 1000D;
        if (hole.progress >= captureSeconds(owner, hole.capturing)) {
            HuntingCreatureMob mob = hole.capturing;
            hole.capturing = null;
            hole.progress = 0;
            hole.expiresAt = System.currentTimeMillis() + LIFETIME_MS;
            mob.capture(owner);
        }
    }

    public static double captureSeconds(SkyBlockPlayer owner, HuntingCreatureMob mob) {
        int level = CreatureCapture.shardOf(mob.spec()).map(AttributeDefinition::rarity).map(rarity -> rarity.ordinal() + 1).orElse(2);
        double pressure = AttributeEffectService.value(owner.getHuntingData(), AttributeId.parse("R9")) / 100D;
        return Math.max(0.5, level * 2D * (1 - pressure));
    }
}
