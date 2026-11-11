package dev.daweapon.type.hub.carnival;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.metadata.monster.zombie.ZombieMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalGame;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalGameResult;
import dev.daweapon.type.skyblockgeneric.carnival.games.ZombieShootoutScore;
import dev.daweapon.type.skyblockgeneric.carnival.games.ZombieShootoutScore.Spawn;
import dev.daweapon.type.skyblockgeneric.carnival.games.ZombieShootoutScore.ZombieTier;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Zombie Shootout as captured on Hypixel: armoured zombies wander the two terraces in growing waves, one Redstone Lamp
 * lights at a time, and the Carnival Dart Tube fires a fast piercing arrow at whatever the player aims at.
 */
public class ZombieShootoutGame extends CarnivalGameBase implements StationaryCarnivalGame {
    private static final Pos START = new Pos(-100.5, 71, 44.5, 45, 0);
    private static final Pos END = new Pos(-96.5, 70, 37.5, 45, 0);
    private static final List<Vec> SPAWNS = List.of(
            new Vec(-114, 71, 43.5), new Vec(-114, 71, 45.5), new Vec(-114, 71, 47.25), new Vec(-113, 71, 49.5),
            new Vec(-112, 71, 51.5), new Vec(-110, 71, 54), new Vec(-107.5, 71, 56), new Vec(-105.5, 71, 57),
            new Vec(-103.5, 71, 58), new Vec(-101.5, 71, 58), new Vec(-99.5, 71, 58),
            new Vec(-117, 72, 43.5), new Vec(-117, 72, 46.5), new Vec(-116, 72, 50.5), new Vec(-113.5, 72, 54.5),
            new Vec(-111.8125, 72, 56.6875), new Vec(-110, 72, 58), new Vec(-106.5, 72, 60), new Vec(-103.5, 72, 61),
            new Vec(-99.5, 72, 61), new Vec(-97, 72, 60.5));
    private static final List<BlockVec> LAMPS = List.of(
            new BlockVec(-96, 76, 61), new BlockVec(-99, 77, 62), new BlockVec(-102, 75, 62), new BlockVec(-106, 77, 61),
            new BlockVec(-109, 75, 60), new BlockVec(-112, 76, 58), new BlockVec(-115, 77, 55), new BlockVec(-117, 76, 52),
            new BlockVec(-118, 76, 49), new BlockVec(-119, 75, 45), new BlockVec(-119, 77, 42), new BlockVec(-118, 76, 39));
    private static final double MIN_X = -117.5, MAX_X = -92.5, MIN_Z = 37.5, MAX_Z = 61.5;
    private static final double ADULT_SPEED = 0.12, BABY_SPEED = 0.18;
    private static final double WANDER_REACH = 10;
    private static final int NAME_SHOWN_TICKS = 60;
    private static final int SHOT_COOLDOWN_TICKS = 10;
    private static final double ARROW_SPEED = 4.5;
    private static final double ARROW_GRAVITY = 0.05, ARROW_DRAG = 0.99;
    private static final double ARROW_HIT_INFLATE = 0.3;
    private static final int ARROW_LIFETIME_TICKS = 30;
    private static final int FIRST_LAMP_TICK = 115;
    private static final int LAMP_QUIET_TICKS = 10;

    private final ZombieShootoutScore score = new ZombieShootoutScore();
    private final List<Zombie> zombies = new ArrayList<>();
    private final List<Dart> darts = new ArrayList<>();
    private final Queue<Pos> shots = new ConcurrentLinkedQueue<>();
    private List<BlockVec> lamps = LAMPS;
    private int wave;
    private long lastShot = -SHOT_COOLDOWN_TICKS;
    private @Nullable BlockVec litLamp;
    private long litAt;
    private long lampOffAt = -LAMP_QUIET_TICKS;
    private long nextLampAt = FIRST_LAMP_TICK;

    public ZombieShootoutGame(SkyBlockPlayer player) {
        super(player, CarnivalGame.ZOMBIE_SHOOTOUT, ZombieShootoutScore.SECONDS);
    }

    @Override
    public Pos station() {
        return START;
    }

    @Override
    protected Pos startPosition() {
        return START;
    }

    @Override
    protected Pos endPosition() {
        return END;
    }

    private final class Zombie {
        final ZombieTier tier;
        final boolean baby;
        final double row;
        final long despawnAt;
        final EntityCreature entity;
        long nextWanderAt;

        Zombie(Spawn spawn, Vec at, Instance instance) {
            this.tier = spawn.tier();
            this.baby = spawn.baby();
            this.row = at.y();
            this.despawnAt = tick + ZombieShootoutScore.ZOMBIE_LIFETIME_TICKS;
            this.entity = new EntityCreature(EntityType.ZOMBIE);
            entity.setAutoViewable(false);
            entity.setInvulnerable(true);
            entity.editEntityMeta(ZombieMeta.class, meta -> meta.setBaby(baby));
            entity.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(baby ? BABY_SPEED : ADULT_SPEED);
            entity.setEquipment(EquipmentSlot.HELMET, ItemStack.of(tier.getHelmet()));
            entity.setEquipment(EquipmentSlot.CHESTPLATE, ItemStack.of(tier.getChestplate()));
            entity.setInstance(instance, new Pos(at, 0, 0)).thenRun(() -> entity.addViewer(player));
        }

        void tick() {
            long left = despawnAt - tick;
            if (left <= NAME_SHOWN_TICKS) {
                entity.setCustomName(Text.of(timerName(left)).asComponent());
                entity.setCustomNameVisible(true);
            }
            if (tick >= nextWanderAt) wander();
        }

        void wander() {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            nextWanderAt = tick + random.nextInt(40, 81);
            Pos at = entity.getPosition();
            List<Vec> reachable = SPAWNS.stream()
                    .filter(spot -> spot.y() == row && spot.withY(0).distance(at.asVec().withY(0)) <= WANDER_REACH)
                    .toList();
            Vec spot = reachable.isEmpty() ? nearestSpot(at) : reachable.get(random.nextInt(reachable.size()));
            Vec target = new Vec(clamp(spot.x() + random.nextDouble(-1.5, 1.5), MIN_X, MAX_X), row,
                    clamp(spot.z() + random.nextDouble(-1.5, 1.5), MIN_Z, MAX_Z));
            entity.getNavigator().setPathTo(target, 0.5, 16, 8, () -> nextWanderAt = Math.min(nextWanderAt, tick + 1));
        }

        Vec nearestSpot(Pos at) {
            Vec best = SPAWNS.getFirst();
            for (Vec spot : SPAWNS) {
                if (spot.y() == row && spot.distance(at.asVec()) < best.distance(at.asVec())) best = spot;
            }
            return best;
        }

        double hitTime(Vec from, Vec to) {
            Pos at = entity.getPosition();
            double halfWidth = (baby ? 0.245 : 0.3) + ARROW_HIT_INFLATE;
            double height = (baby ? 0.98 : 1.95) + ARROW_HIT_INFLATE;
            return segmentBoxEntry(from, to,
                    new Vec(at.x() - halfWidth, at.y() - ARROW_HIT_INFLATE, at.z() - halfWidth),
                    new Vec(at.x() + halfWidth, at.y() + height, at.z() + halfWidth));
        }
    }

    private final class Dart {
        final Entity entity;
        final long firedAt;
        Vec position;
        Vec velocity;

        Dart(Pos look, Instance instance) {
            this.firedAt = tick;
            this.position = look.asVec().add(0, player.getEyeHeight() - 0.1, 0);
            this.velocity = look.direction().mul(ARROW_SPEED);
            this.entity = new Entity(EntityType.ARROW);
            entity.setNoGravity(true);
            entity.setAutoViewable(false);
            entity.setInstance(instance, facing(position, velocity)).thenRun(() -> entity.addViewer(player));
        }

        boolean fly(Instance instance) {
            Vec next = position.add(velocity);
            double blockAt = 1;
            BlockVec blockHit = null;
            double length = velocity.length();
            for (double travelled = 0; travelled <= length; travelled += 0.1) {
                Vec point = position.add(velocity.mul(travelled / length));
                BlockVec block = new BlockVec(point.blockX(), point.blockY(), point.blockZ());
                if (block.equals(litLamp) || instance.getBlock(block).solid()) {
                    blockAt = travelled / length;
                    blockHit = block;
                    break;
                }
            }
            for (Zombie zombie : List.copyOf(zombies)) {
                double entry = zombie.hitTime(position, next);
                if (entry >= 0 && entry <= blockAt) shoot(zombie);
            }
            if (blockHit != null) {
                if (blockHit.equals(litLamp)) shootLamp();
                return false;
            }
            position = next;
            velocity = velocity.mul(ARROW_DRAG).sub(0, ARROW_GRAVITY, 0);
            entity.teleport(facing(position, velocity));
            return tick - firedAt < ARROW_LIFETIME_TICKS;
        }
    }

    @Override
    public void onRightClick() {
        shots.add(player.getPosition());
    }

    @Override
    protected void onStart() {
        Instance instance = player.getInstance();
        if (instance == null) return;
        List<BlockVec> real = LAMPS.stream().filter(lamp -> instance.getBlock(lamp).compare(Block.REDSTONE_LAMP)).toList();
        if (!real.isEmpty()) lamps = real;
        spawnWave(instance);
    }

    @Override
    protected void onTick() {
        Instance instance = player.getInstance();
        if (instance == null) return;
        Pos look;
        while ((look = shots.poll()) != null) fire(look, instance);
        darts.removeIf(dart -> {
            if (dart.fly(instance)) return false;
            dart.entity.remove();
            return true;
        });
        for (Zombie zombie : List.copyOf(zombies)) {
            if (tick >= zombie.despawnAt) expire(zombie);
            else zombie.tick();
        }
        if (zombies.isEmpty() && !timeUp()) spawnWave(instance);
        tickLamps();
    }

    private void fire(Pos look, Instance instance) {
        if (tick - lastShot < SHOT_COOLDOWN_TICKS) return;
        lastShot = tick;
        darts.add(new Dart(look, instance));
        playAt("block.note_block.snare", Sound.Source.BLOCK, 1f, 1f);
        playAt("entity.bat.takeoff", Sound.Source.NEUTRAL, 0.8f, 1.4920635f);
    }

    private void spawnWave(Instance instance) {
        wave++;
        List<Vec> spots = new ArrayList<>(SPAWNS);
        Collections.shuffle(spots);
        List<Spawn> spawns = ZombieShootoutScore.wave(wave, ThreadLocalRandom.current());
        for (int i = 0; i < spawns.size(); i++) zombies.add(new Zombie(spawns.get(i), spots.get(i), instance));
        playAt("entity.zombie_villager.converted", Sound.Source.HOSTILE, 1f, 1f);
    }

    private void shoot(Zombie zombie) {
        if (!zombies.remove(zombie)) return;
        zombie.entity.remove();
        gain(score.onZombie(zombie.tier, zombie.baby, tick));
        playAt("entity.arrow.hit_player", Sound.Source.NEUTRAL, 1f, 1f);
    }

    private void expire(Zombie zombie) {
        zombies.remove(zombie);
        Pos at = zombie.entity.getPosition();
        player.sendPacket(new ParticlePacket(Particle.CLOUD, true, true, at.x(), at.y(), at.z(), 0.3f, 1.5f, 0.3f, 0f, 20));
        zombie.entity.remove();
    }

    private void tickLamps() {
        if (litLamp != null && tick - litAt >= ZombieShootoutScore.LAMP_LIT_TICKS) {
            turnOffLamp();
            playAt("block.lever.click", Sound.Source.BLOCK, 1f, 0.74603176f);
        }
        if (tick < nextLampAt) return;
        nextLampAt += ZombieShootoutScore.LAMP_CYCLE_TICKS;
        if (timeUp() || litLamp != null || tick - lampOffAt < LAMP_QUIET_TICKS) return;
        litLamp = lamps.get(ThreadLocalRandom.current().nextInt(lamps.size()));
        litAt = tick;
        showBlock(litLamp, Block.REDSTONE_LAMP.withProperty("lit", "true"));
        playAt("block.lever.click", Sound.Source.BLOCK, 1f, 1.2380953f);
    }

    private void shootLamp() {
        turnOffLamp();
        gain(score.onLamp(tick));
        playAt("entity.arrow.hit_player", Sound.Source.NEUTRAL, 1f, 1f);
    }

    private void turnOffLamp() {
        Instance instance = player.getInstance();
        if (litLamp == null) return;
        showBlock(litLamp, instance == null ? Block.REDSTONE_LAMP : instance.getBlock(litLamp));
        litLamp = null;
        lampOffAt = tick;
    }

    static String timerName(long ticksLeft) {
        long tenths = Math.max(0, ticksLeft) / 2;
        String colour = ticksLeft > 40 ? "<a>" : tenths >= 10 ? "<e>" : "<c>";
        return colour + tenths / 10 + "." + tenths % 10 + "s";
    }

    static double segmentBoxEntry(Vec from, Vec to, Vec min, Vec max) {
        double enter = 0, exit = 1;
        double[] start = {from.x(), from.y(), from.z()};
        double[] delta = {to.x() - from.x(), to.y() - from.y(), to.z() - from.z()};
        double[] low = {min.x(), min.y(), min.z()};
        double[] high = {max.x(), max.y(), max.z()};
        for (int axis = 0; axis < 3; axis++) {
            if (Math.abs(delta[axis]) < 1e-9) {
                if (start[axis] < low[axis] || start[axis] > high[axis]) return -1;
                continue;
            }
            double a = (low[axis] - start[axis]) / delta[axis];
            double b = (high[axis] - start[axis]) / delta[axis];
            enter = Math.max(enter, Math.min(a, b));
            exit = Math.min(exit, Math.max(a, b));
            if (enter > exit) return -1;
        }
        return enter;
    }

    private static Pos facing(Vec position, Vec velocity) {
        double horizontal = Math.sqrt(velocity.x() * velocity.x() + velocity.z() * velocity.z());
        float yaw = (float) Math.toDegrees(Math.atan2(velocity.x(), velocity.z()));
        float pitch = (float) Math.toDegrees(Math.atan2(velocity.y(), horizontal));
        return new Pos(position, yaw, pitch);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private void playAt(String sound, Sound.Source source, float volume, float pitch) {
        Pos at = player.getPosition();
        player.playSound(Sound.sound(Key.key(sound), source, volume, pitch), at.x(), at.y(), at.z());
    }

    @Override
    protected List<String> sidebarBody() {
        return List.of("<f>Kills: <a>" + score.totalKills());
    }

    @Override
    protected String gainLabel(long gain) {
        return "(" + StringUtility.commaify(gain) + ")";
    }

    @Override
    protected long score() {
        return score.getScore();
    }

    @Override
    protected void onEnd() {
        darts.forEach(dart -> dart.entity.remove());
        darts.clear();
        zombies.forEach(zombie -> zombie.entity.remove());
        zombies.clear();
        if (litLamp != null) {
            litLamp = null;
            playAt("block.lever.click", Sound.Source.BLOCK, 1f, 0.74603176f);
        }
    }

    @Override
    protected CarnivalGameResult result() {
        return score.result();
    }

    @Override
    protected List<String> summary(CarnivalGameResult result) {
        String spacer = "<f>" + " ".repeat(40);
        List<String> lines = new ArrayList<>();
        lines.add("<f>" + " ".repeat(CarnivalChat.spacesFor("<f>Zombies Slain")) + "Zombies Slain");
        for (ZombieTier tier : ZombieTier.values()) {
            lines.add(CarnivalChat.centered("<a>" + score.kills(tier) + "x <r>" + tier.getColor() + tier.getDisplayName()
                    + " <7>- " + StringUtility.commaify(score.points(tier)) + " points"));
        }
        lines.add(spacer);
        lines.add(CarnivalChat.centered("<c><l>BONUS! <c>" + score.getLamps() + "x Redstone Lamps <7>- "
                + StringUtility.commaify((long) score.getLamps() * ZombieShootoutScore.LAMP_POINTS) + " points"));
        lines.add(spacer);
        lines.add(CarnivalChat.centered("<f>Total Kills: <a>" + score.totalKills()));
        return lines;
    }
}
