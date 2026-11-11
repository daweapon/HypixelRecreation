package dev.daweapon.type.hub.carnival;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.entity.metadata.other.FishingHookMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalGame;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalGameResult;
import dev.daweapon.type.skyblockgeneric.carnival.games.CatchAFishScore;
import dev.daweapon.type.skyblockgeneric.carnival.games.CatchAFishScore.FishColor;
import dev.daweapon.type.skyblockgeneric.entity.FishingHook;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockActionBar;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockActionBar.BarSection;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Catch a Fish at the pond by the Carnival Fisherman, as captured on Hypixel: ten head-on-armour-stand fish swim from
 * fixed spots, a thrown bobber hooks one it touches, the reel bar above it fills at the fish's own rate, then the fish
 * is flung out of the pond and its points land a second later.
 */
public class CatchAFishGame extends CarnivalGameBase implements StationaryCarnivalGame {
    private static final Pos START = new Pos(-78.0, 72, 39.0, 15, 0);
    private static final Pos END = new Pos(-78.5, 72, 28.5, 60, 0);
    private static final double FISH_Y = 68.375;
    private static final double HEAD_HEIGHT = 1.66;
    private static final List<Vec> SPOTS = List.of(
            new Vec(-76.5, FISH_Y, 43), new Vec(-74.5, FISH_Y, 48.5), new Vec(-71.5, FISH_Y, 45),
            new Vec(-71, FISH_Y, 39), new Vec(-81, FISH_Y, 41.5), new Vec(-78.5, FISH_Y, 50),
            new Vec(-81, FISH_Y, 48), new Vec(-74.5, FISH_Y, 35.5), new Vec(-78.5, FISH_Y, 47),
            new Vec(-82.5, FISH_Y, 45));
    private static final double MIN_X = -84.3, MAX_X = -69.1, MIN_Z = 33.9, MAX_Z = 51.8;
    private static final int LIFETIME_TICKS = 300;
    private static final int RESPAWN_MIN_TICKS = 40, RESPAWN_MAX_TICKS = 60;
    private static final double SWIM_SPEED = 0.06;
    private static final double BOB_AMPLITUDE = 0.2;
    private static final double HOOK_REACH = 0.6;
    private static final int NAME_DELAY_TICKS = 2;
    private static final int FLING_TICKS = 20;
    private static final int MESSAGE_TICKS = 20;
    private static final int BOBBER_RANGE = 40;

    private enum State { SWIMMING, HOOKED, REELED, FLUNG }

    private final CatchAFishScore score = new CatchAFishScore();
    private final List<Fish> fish = new ArrayList<>();
    private final long[] respawnAt = new long[SPOTS.size()];
    private final Queue<Runnable> clicks = new ConcurrentLinkedQueue<>();
    private final Queue<String> messages = new ArrayDeque<>();
    private long nextMessageAt;
    private @Nullable Bobber bobber;
    private long lastHook;

    public CatchAFishGame(SkyBlockPlayer player) {
        super(player, CarnivalGame.CATCH_A_FISH, CatchAFishScore.SECONDS);
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

    private final class Fish {
        final FishColor color;
        final int spot;
        final LivingEntity stand;
        final long despawnAt;
        State state = State.SWIMMING;
        Vec position;
        Vec target;
        double bobPhase = ThreadLocalRandom.current().nextDouble(Math.PI * 2);
        long stateSince;
        int update;
        Vec flingDirection = Vec.ZERO;

        Fish(int spot, Instance instance) {
            this.spot = spot;
            this.color = FishColor.roll(ThreadLocalRandom.current());
            this.position = SPOTS.get(spot);
            this.target = randomPondSpot();
            this.despawnAt = tick + LIFETIME_TICKS + ThreadLocalRandom.current().nextInt(7);
            this.stand = new LivingEntity(EntityType.ARMOR_STAND);
            stand.setNoGravity(true);
            stand.setInvisible(true);
            stand.setSilent(true);
            stand.setAutoViewable(false);
            stand.setHelmet(ItemStacks.head(color.getTexture(), Text.empty(), List.of()).build());
            stand.setInstance(instance, new Pos(position, 0, 0)).thenRun(() -> stand.addViewer(player));
        }

        Vec head() {
            return position.add(0, HEAD_HEIGHT, 0);
        }

        void swim() {
            if (position.withY(0).distance(target.withY(0)) < 0.3 || ThreadLocalRandom.current().nextInt(30) == 0) {
                target = randomPondSpot();
            }
            Vec step = target.sub(position).withY(0);
            double speed = state == State.SWIMMING ? SWIM_SPEED : SWIM_SPEED / 3;
            if (step.length() > speed) step = step.normalize().mul(speed);
            Vec next = position.add(step);
            if (!inPond(next.x(), next.z())) {
                target = randomPondSpot();
                return;
            }
            bobPhase += 0.15;
            position = next.withY(FISH_Y + Math.sin(bobPhase) * BOB_AMPLITUDE);
            float yaw = step.isZero() ? stand.getPosition().yaw() : (float) Math.toDegrees(Math.atan2(-step.x(), step.z()));
            stand.teleport(new Pos(position, yaw, 0));
        }

        void name(String markup) {
            stand.setCustomName(Text.of(markup).asComponent());
            stand.setCustomNameVisible(true);
        }

        void headPose(Vec rotation) {
            stand.editEntityMeta(ArmorStandMeta.class, meta -> meta.setHeadRotation(rotation));
        }
    }

    private final class Bobber {
        final Entity entity;
        Vec position;
        Vec velocity;
        boolean resting;
        @Nullable Fish hooked;

        Bobber(Instance instance) {
            Pos look = player.getPosition();
            this.position = eye();
            this.velocity = FishingHook.castVelocity(look).div(20);
            this.entity = new Entity(EntityType.FISHING_BOBBER);
            entity.editEntityMeta(FishingHookMeta.class, meta -> meta.setOwnerEntity(player));
            entity.setNoGravity(true);
            entity.setAutoViewable(false);
            entity.setInstance(instance, new Pos(position)).thenRun(() -> entity.addViewer(player));
        }

        void move() {
            if (hooked != null) {
                position = hooked.head();
                entity.teleport(new Pos(position));
                return;
            }
            Instance instance = player.getInstance();
            if (instance == null) return;
            if (!resting) {
                Vec next = position.add(velocity);
                velocity = velocity.mul(0.92).sub(0, 0.03, 0);
                Block block = instance.getBlock(next);
                if (block.compare(Block.WATER)) {
                    next = next.withY(Math.floor(next.y()) + 0.9);
                    velocity = velocity.mul(0.3, 0, 0.3);
                    resting = true;
                } else if (block.isSolid()) {
                    next = position;
                    velocity = Vec.ZERO;
                    resting = true;
                }
                Fish touched = touched(position, next);
                position = next;
                if (touched != null) {
                    hook(touched);
                    return;
                }
            } else {
                velocity = velocity.mul(0.8);
                Vec next = position.add(velocity);
                if (instance.getBlock(next).compare(Block.WATER)) position = next;
                Fish touched = touched(position, position);
                if (touched != null) {
                    hook(touched);
                    return;
                }
            }
            entity.teleport(new Pos(position));
        }

        boolean lost() {
            return position.y() < FISH_Y - 8 || position.distance(START.asVec()) > BOBBER_RANGE;
        }

        void remove() {
            entity.remove();
        }
    }

    @Override
    public void onRightClick() {
        clicks.add(this::click);
    }

    private void click() {
        if (bobber != null) {
            if (bobber.hooked != null) return;
            bobber.remove();
            bobber = null;
            return;
        }
        Instance instance = player.getInstance();
        if (timeUp() || instance == null) return;
        bobber = new Bobber(instance);
        playAt("entity.arrow.shoot", Sound.Source.NEUTRAL, 0.5f,
                0.4f / (ThreadLocalRandom.current().nextFloat() * 0.4f + 0.8f), eye());
    }

    private @Nullable Fish touched(Vec from, Vec to) {
        for (Fish candidate : fish) {
            if (candidate.state != State.SWIMMING) continue;
            Vec head = candidate.head();
            if (head.distance(to) <= HOOK_REACH || head.distance(from.add(to).div(2)) <= HOOK_REACH) return candidate;
        }
        return null;
    }

    private void hook(Fish target) {
        if (bobber == null) return;
        bobber.hooked = target;
        bobber.position = target.head();
        bobber.entity.teleport(new Pos(bobber.position));
        bobber.entity.editEntityMeta(FishingHookMeta.class, meta -> meta.setHookedEntity(target.stand));
        target.state = State.HOOKED;
        target.stateSince = tick;
        target.update = 0;
        lastHook = tick;
        playAt("entity.player.splash", Sound.Source.MASTER, 1f, 0.4920635f, target.head());
    }

    @Override
    protected void onStart() {
        Instance instance = player.getInstance();
        if (instance == null) return;
        for (int spot = 0; spot < SPOTS.size(); spot++) fish.add(new Fish(spot, instance));
    }

    @Override
    protected void onTick() {
        Runnable click;
        while ((click = clicks.poll()) != null) click.run();
        if (bobber != null) {
            bobber.move();
            if (bobber != null && bobber.hooked == null && bobber.lost()) removeBobber();
        }
        for (Fish swimmer : List.copyOf(fish)) tickFish(swimmer);
        respawn();
        checkStreakTimeout();
        showMessages();
    }

    private void tickFish(Fish swimmer) {
        switch (swimmer.state) {
            case SWIMMING -> {
                if (tick >= swimmer.despawnAt) {
                    leave(swimmer);
                    return;
                }
                swimmer.swim();
            }
            case HOOKED -> {
                swimmer.swim();
                long elapsed = tick - swimmer.stateSince - 1;
                if (elapsed < 0 || elapsed % CatchAFishScore.BAR_UPDATE_TICKS != 0) return;
                reelStep(swimmer);
            }
            case REELED -> {
                if (tick - swimmer.stateSince >= NAME_DELAY_TICKS) fling(swimmer);
            }
            case FLUNG -> {
                long flown = tick - swimmer.stateSince;
                if (flown >= FLING_TICKS) {
                    credit(swimmer);
                    return;
                }
                double horizontal = 0.2 + 0.06 * flown;
                double up = 0.1 + 0.06 * flown;
                swimmer.position = swimmer.position.add(swimmer.flingDirection.mul(horizontal)).add(0, up, 0);
                swimmer.stand.teleport(new Pos(swimmer.position, swimmer.stand.getPosition().yaw(), 0));
            }
        }
    }

    private void reelStep(Fish swimmer) {
        int update = swimmer.update++;
        FishColor color = swimmer.color;
        swimmer.name(color.bar(update));
        playAt("block.note_block.hat", Sound.Source.BLOCK, 0.75f, color.hatPitch(update), eye());
        if (color.isReeled(update)) {
            swimmer.state = State.REELED;
            swimmer.stateSince = tick;
            return;
        }
        ThreadLocalRandom random = ThreadLocalRandom.current();
        swimmer.headPose(new Vec(random.nextDouble(-10, 0), random.nextDouble(-5, 5), 0));
    }

    private void fling(Fish swimmer) {
        swimmer.name("<6>+" + swimmer.color.getPoints() + " points");
        swimmer.headPose(Vec.ZERO);
        playAt("entity.player.levelup", Sound.Source.PLAYER, 0.55f, 2f, eye());
        playAt("entity.bat.takeoff", Sound.Source.NEUTRAL, 0.5f, 1f, eye());
        removeBobber();
        Vec toPlayer = player.getPosition().asVec().sub(swimmer.position).withY(0);
        swimmer.flingDirection = toPlayer.isZero() ? new Vec(0, 0, -1) : toPlayer.normalize();
        swimmer.state = State.FLUNG;
        swimmer.stateSince = tick;
    }

    private void credit(Fish swimmer) {
        leave(swimmer);
        if (timeUp()) return;
        boolean doubled = ThreadLocalRandom.current().nextDouble() < CatchAFishScore.DOUBLE_POINTS_CHANCE;
        int points = score.onCatch(swimmer.color, doubled);
        gain(points);
        int streak = score.getStreak();
        if (streak >= 2) messages.add("<3><l>STREAK! <b>" + streak + "x <7>(+" + score.streakBonus() + " pts)");
        if (doubled) messages.add("<6><l>DOUBLE POINTS! <e>+" + points);
    }

    private void leave(Fish swimmer) {
        swimmer.stand.remove();
        fish.remove(swimmer);
        if (bobber != null && bobber.hooked == swimmer) removeBobber();
        respawnAt[swimmer.spot] = tick + ThreadLocalRandom.current().nextInt(RESPAWN_MIN_TICKS, RESPAWN_MAX_TICKS + 1);
    }

    private void respawn() {
        Instance instance = player.getInstance();
        if (timeUp() || instance == null) return;
        for (int spot = 0; spot < SPOTS.size(); spot++) {
            if (respawnAt[spot] == 0 || tick < respawnAt[spot]) continue;
            respawnAt[spot] = 0;
            fish.add(new Fish(spot, instance));
        }
    }

    private void checkStreakTimeout() {
        if (score.getStreak() == 0 || tick - lastHook < CatchAFishScore.STREAK_TIMEOUT_TICKS) return;
        boolean catching = fish.stream().anyMatch(swimmer -> swimmer.state != State.SWIMMING);
        if (catching) return;
        if (score.getStreak() >= 2) messages.add("<c><l>STREAK RESET!");
        score.breakStreak();
    }

    private void showMessages() {
        if (messages.isEmpty() || tick < nextMessageAt) return;
        String message = messages.poll();
        nextMessageAt = tick + MESSAGE_TICKS;
        SkyBlockActionBar bar = SkyBlockActionBar.getFor(player);
        for (BarSection section : BarSection.values()) {
            bar.addReplacement(section, section == BarSection.HEALTH ? Text.of(message) : Text.empty(), MESSAGE_TICKS, 1000);
        }
    }

    private void removeBobber() {
        if (bobber == null) return;
        bobber.remove();
        bobber = null;
    }

    private Vec randomPondSpot() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int attempt = 0; attempt < 20; attempt++) {
            double x = random.nextDouble(MIN_X, MAX_X);
            double z = random.nextDouble(MIN_Z, MAX_Z);
            if (inPond(x, z)) return new Vec(x, FISH_Y, z);
        }
        return SPOTS.get(random.nextInt(SPOTS.size()));
    }

    private boolean inPond(double x, double z) {
        if (x < MIN_X || x > MAX_X || z < MIN_Z || z > MAX_Z) return false;
        Instance instance = player.getInstance();
        if (instance == null) return false;
        for (int y = 67; y <= 69; y++) {
            if (instance.getBlock(new Vec(x, y, z)).compare(Block.WATER)) return true;
        }
        return false;
    }

    private void playAt(String sound, Sound.Source source, float volume, float pitch, Point at) {
        player.playSound(Sound.sound(Key.key(sound), source, volume, pitch), at.x(), at.y(), at.z());
    }

    @Override
    protected List<String> sidebarBody() {
        return List.of("<f>Catch Streak: <a>" + score.getStreak());
    }

    @Override
    protected long score() {
        return score.getScore();
    }

    @Override
    protected void onEnd() {
        removeBobber();
        fish.forEach(swimmer -> swimmer.stand.remove());
        fish.clear();
        messages.clear();
    }

    @Override
    protected CarnivalGameResult result() {
        return score.result();
    }

    @Override
    protected List<String> summary(CarnivalGameResult result) {
        List<String> lines = new ArrayList<>();
        lines.add(CarnivalChat.centered("<f>Fish Caught"));
        for (FishColor color : FishColor.values()) {
            int count = score.caught(color);
            if (count > 0) lines.add(CarnivalChat.centered("<a>" + count + "x " + color.getColor() + color.getDisplayName()));
        }
        if (lines.size() == 1) lines.add(CarnivalChat.centered("<7>None"));
        return lines;
    }
}
