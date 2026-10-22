package dev.daweapon.type.galatea.npcs.oden;

import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.hunting.creature.BlackHoleService;
import dev.daweapon.type.skyblockgeneric.hunting.creature.CreatureCatalog;
import dev.daweapon.type.skyblockgeneric.hunting.creature.CreatureSpawner;
import dev.daweapon.type.skyblockgeneric.hunting.creature.CreatureSpec;
import dev.daweapon.type.skyblockgeneric.hunting.creature.Huntaxes;
import dev.daweapon.type.skyblockgeneric.hunting.creature.HuntingCreatureMob;
import dev.daweapon.type.skyblockgeneric.hunting.creature.special.CreatureSpecial;
import dev.daweapon.type.skyblockgeneric.hunting.creature.special.CreatureSpecials;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.TaskSchedule;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class OdenChillshotQuest {
    public static final String SPECIAL_ID = "ODEN_CHILLSHOT";
    public static final String DONE_TRIGGER = "npc.oden.chillshot";
    private static final double HOLE_RADIUS = 14;
    private static final double CAPTURE_DISTANCE = 2.2;
    private static final double PULL_SPEED = 5;
    private static final long TIMEOUT_MS = 300_000;
    private static final int TICK_STEP = 5;

    private final Pos odenPosition;
    private final Consumer<SkyBlockPlayer> onCaptured;
    private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();
    private boolean ticking;

    private static final class Session {
        private final HuntingCreatureMob mob;
        private final Pos hole;
        private final long expiresAt = System.currentTimeMillis() + TIMEOUT_MS;
        private double progress;

        private Session(HuntingCreatureMob mob, Pos hole) {
            this.mob = mob;
            this.hole = hole;
        }
    }

    public OdenChillshotQuest(Pos odenPosition, Consumer<SkyBlockPlayer> onCaptured) {
        this.odenPosition = odenPosition;
        this.onCaptured = onCaptured;
        CreatureSpecials.register(SPECIAL_ID, new CreatureSpecial() {
            @Override
            public boolean onPlayerHit(SkyBlockPlayer player, HuntingCreatureMob mob) {
                if (Huntaxes.heldBy(player) != null) return false;
                player.sendMessage("<e>[NPC] <b>Oden<f>: You can only damage it using an <a>Huntaxe<f>!");
                return true;
            }
        });
    }

    public boolean active(SkyBlockPlayer player) {
        Session session = sessions.get(player.getUuid());
        return session != null && !session.mob.isDead() && !session.mob.isRemoved();
    }

    public void start(SkyBlockPlayer player) {
        if (active(player) || player.getInstance() == null) return;
        Instance instance = player.getInstance();
        Pos hole = anchor(instance, 2, 3, 1.5);
        Pos spawn = anchor(instance, 7, 3, 0.1);
        HuntingCreatureMob mob = HuntingCreatureMob.create(spec());
        mob.setInstance(instance, spawn);
        mob.setHeader("<7>" + player.getUsername() + "'s");
        sessions.put(player.getUuid(), new Session(mob, hole));
        startTicking();
    }

    private Pos anchor(Instance instance, double dx, double dz, double lift) {
        Pos ground = CreatureSpawner.surface(instance, (int) Math.floor(odenPosition.x() + dx), (int) Math.floor(odenPosition.z() + dz),
                false, (int) odenPosition.y() + 6);
        Pos base = ground == null ? odenPosition.add(dx, 0, dz) : ground;
        return base.add(0, lift, 0);
    }

    private static CreatureSpec spec() {
        CreatureSpec base = CreatureCatalog.byId("CHILLSHOT").orElseThrow();
        return new CreatureSpec(base.id(), base.name(), base.island(), base.kind(), base.level(), base.health(), base.damage(),
                base.speed(), base.entity(), base.mobTypes(), base.shard(), base.capture(), base.staminaBars(), base.minNet(),
                SPECIAL_ID, base.medium(), CreatureSpec.Behavior.STATIONARY, base.xp(), base.orbs(), base.coins(), base.drops(),
                base.shardChance(), base.bestiary(), base.spawn(), base.messages(), base.notes(), base.altCapture(),
                base.netDamage(), base.raw());
    }

    private void startTicking() {
        if (ticking) return;
        ticking = true;
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            Iterator<Map.Entry<UUID, Session>> iterator = sessions.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<UUID, Session> entry = iterator.next();
                SkyBlockPlayer owner = SkyBlockGenericLoader.getLoadedPlayers().stream()
                        .filter(player -> player.getUuid().equals(entry.getKey())).findFirst().orElse(null);
                Session session = entry.getValue();
                boolean gone = session.mob.isDead() || session.mob.isRemoved();
                if (owner == null || gone || System.currentTimeMillis() > session.expiresAt) {
                    if (!gone) session.mob.remove();
                    iterator.remove();
                    continue;
                }
                if (tick(owner, session)) iterator.remove();
            }
            return TaskSchedule.tick(TICK_STEP);
        });
    }

    private boolean tick(SkyBlockPlayer owner, Session session) {
        HuntingCreatureMob mob = session.mob;
        mob.getInstance().sendGroupedPacket(new ParticlePacket(Particle.PORTAL, false, false, session.hole.x(), session.hole.y(),
                session.hole.z(), 0.4f, 0.4f, 0.4f, 0.5f, 12));
        if (!mob.atHuntFloor() || mob.getPosition().distance(session.hole) > HOLE_RADIUS) {
            session.progress = 0;
            return false;
        }
        Vec pull = Vec.fromPoint(session.hole.sub(mob.getPosition()));
        if (pull.length() > CAPTURE_DISTANCE) {
            double step = Math.min(pull.length() - CAPTURE_DISTANCE + 0.1, PULL_SPEED * TICK_STEP / 20D);
            mob.teleport(mob.getPosition().add(pull.normalize().mul(step)));
            session.progress = 0;
            return false;
        }
        session.progress += TICK_STEP * 50 / 1000D;
        if (session.progress < BlackHoleService.captureSeconds(owner, mob)) return false;
        mob.capture(owner);
        ChapterTriggers.fire(owner, DONE_TRIGGER);
        onCaptured.accept(owner);
        return true;
    }
}
