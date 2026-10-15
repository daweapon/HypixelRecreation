package dev.daweapon.type.hub.carnival;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.color.DyeColor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.FishingHookMeta;
import net.minestom.server.entity.metadata.water.fish.TropicalFishMeta;
import net.minestom.server.instance.block.Block;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalGame;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalGameResult;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalGameResult.Metric;
import dev.daweapon.type.skyblockgeneric.carnival.games.CatchAFishScore;
import dev.daweapon.type.skyblockgeneric.carnival.games.CatchAFishScore.FishColor;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Catch a Fish in the mountain pond south of the Carnival Fisherman. Fish swim erratically and
 * despawn; right-click hooks the fish nearest to where the line lands, and holding right-click
 * (the client repeats the use every few ticks) reels it in until its bar fills.
 */
public class CatchAFishGame extends CarnivalGameBase implements StationaryCarnivalGame {
    private static final Pos STATION = new Pos(-86.5, 72, 37.5, 0, 20);
    private static final int WATER_TOP = 66;
    private static final double FISH_Y = 66.35;
    private static final int MIN_X = -94, MAX_X = -76, MIN_Z = 42, MAX_Z = 52;
    private static final int FISH_IN_POND = 6;
    private static final double HOOK_RADIUS = 2.0;
    private static final long HOLD_GRACE_TICKS = 6;
    private static final double FISH_SPEED = 0.12;
    private static final Map<FishColor, DyeColor> COLORS = Map.of(
            FishColor.RED, DyeColor.RED, FishColor.BLUE, DyeColor.BLUE, FishColor.YELLOW, DyeColor.YELLOW);

    private final CatchAFishScore score = new CatchAFishScore();
    private final List<Fish> fish = new ArrayList<>();
    private @Nullable Fish hooked;
    private @Nullable Entity bobber;
    private long lastUse = -100;
    private long lastHook;

    public CatchAFishGame(SkyBlockPlayer player) {
        super(player, CarnivalGame.CATCH_A_FISH, CatchAFishScore.SECONDS);
    }

    @Override
    public Pos station() {
        return STATION;
    }

    private final class Fish {
        final FishColor color;
        final Entity entity;
        final long despawnAt;
        Vec target;
        double progress;

        Fish(FishColor color, Pos spawn, long lifetime) {
            this.color = color;
            this.despawnAt = tick + lifetime;
            this.target = spawn.asVec();
            this.entity = new Entity(EntityType.TROPICAL_FISH);
            entity.setNoGravity(true);
            entity.setAutoViewable(false);
            entity.editEntityMeta(TropicalFishMeta.class, meta -> meta.setVariant(
                    new TropicalFishMeta.Variant(TropicalFishMeta.Pattern.KOB, COLORS.get(color), COLORS.get(color))));
            entity.setInstance(player.getInstance(), spawn).thenRun(() -> entity.addViewer(player));
        }

        void swim() {
            Pos at = entity.getPosition();
            if (at.asVec().distance(target) < 0.3 || ThreadLocalRandom.current().nextInt(25) == 0) target = randomWaterSpot();
            Vec step = target.sub(at.asVec());
            double speed = this == hooked ? FISH_SPEED / 3 : FISH_SPEED;
            if (step.length() > speed) step = step.normalize().mul(speed);
            Vec next = at.asVec().add(step);
            float yaw = (float) Math.toDegrees(Math.atan2(-step.x(), step.z()));
            entity.teleport(new Pos(next.x(), FISH_Y, next.z(), yaw, 0));
        }

        void showProgress() {
            int filled = (int) Math.round(progress / 10);
            entity.setCustomName(Text.of(color.getColor() + "▌".repeat(filled) + "<8>" + "▌".repeat(10 - filled)).asComponent());
            entity.setCustomNameVisible(true);
        }
    }

    @Override
    protected void onStart() {
        player.teleport(STATION);
        for (int i = 0; i < FISH_IN_POND; i++) spawnFish();
        player.sendMessage("<3>Catch a Fish <7>has started! <e>Right-click <7>to hook a fish, then <e>hold Right-click <7>to reel it in!");
    }

    private void spawnFish() {
        Vec spot = randomWaterSpot();
        long lifetime = 20L * ThreadLocalRandom.current().nextInt(8, 15);
        fish.add(new Fish(FishColor.roll(ThreadLocalRandom.current()), new Pos(spot.x(), FISH_Y, spot.z()), lifetime));
    }

    private Vec randomWaterSpot() {
        for (int attempt = 0; attempt < 20; attempt++) {
            int x = ThreadLocalRandom.current().nextInt(MIN_X, MAX_X + 1);
            int z = ThreadLocalRandom.current().nextInt(MIN_Z, MAX_Z + 1);
            if (player.getInstance().getBlock(x, WATER_TOP, z).compare(Block.WATER)) return new Vec(x + 0.5, FISH_Y, z + 0.5);
        }
        return new Vec(-85.5, FISH_Y, 47.5);
    }

    @Override
    public void onRightClick() {
        lastUse = tick;
        if (hooked != null) return;
        Vec landing = landingSpot();
        if (landing == null) return;
        Fish nearest = null;
        for (Fish candidate : fish) {
            double distance = candidate.entity.getPosition().asVec().withY(FISH_Y).distance(landing);
            if (distance <= HOOK_RADIUS && (nearest == null
                    || distance < nearest.entity.getPosition().asVec().distance(landing))) nearest = candidate;
        }
        if (nearest == null) {
            player.playSound(Sound.sound(Key.key("entity.fishing_bobber.splash"), Sound.Source.PLAYER, 0.6f, 1.2f));
            return;
        }
        hook(nearest);
    }

    private @Nullable Vec landingSpot() {
        Vec eye = eye();
        Vec look = look();
        if (look.y() >= -0.02) return null;
        double distance = (eye.y() - (WATER_TOP + 1)) / -look.y();
        return eye.add(look.mul(distance)).withY(FISH_Y);
    }

    private void hook(Fish target) {
        hooked = target;
        lastHook = tick;
        target.progress = 0;
        target.showProgress();
        bobber = new Entity(EntityType.FISHING_BOBBER);
        bobber.setNoGravity(true);
        bobber.setAutoViewable(false);
        bobber.editEntityMeta(FishingHookMeta.class, meta -> meta.setOwnerEntity(player));
        Entity hook = bobber;
        hook.setInstance(player.getInstance(), target.entity.getPosition()).thenRun(() -> hook.addViewer(player));
        player.playSound(Sound.sound(Key.key("entity.fishing_bobber.throw"), Sound.Source.PLAYER, 0.8f, 1f));
    }

    @Override
    protected void onTick() {
        for (Fish swimmer : List.copyOf(fish)) {
            if (tick >= swimmer.despawnAt) {
                if (swimmer == hooked) {
                    player.sendMessage("<c>The fish got away!");
                    score.breakStreak();
                    unhook();
                }
                remove(swimmer);
                continue;
            }
            swimmer.swim();
        }
        while (fish.size() < FISH_IN_POND) spawnFish();
        reel();
        if (hooked == null && score.getStreak() > 0 && tick - lastHook > CatchAFishScore.STREAK_TIMEOUT_TICKS) {
            score.breakStreak();
        }
    }

    private void reel() {
        if (hooked == null) return;
        if (bobber != null) bobber.teleport(hooked.entity.getPosition());
        int reelTicks = hooked.color.getReelTicks();
        boolean holding = tick - lastUse <= HOLD_GRACE_TICKS;
        hooked.progress = Math.max(0, hooked.progress + (holding ? 100D / reelTicks : -50D / reelTicks));
        hooked.showProgress();
        if (hooked.progress < 100) return;
        Fish caught = hooked;
        boolean doubled = ThreadLocalRandom.current().nextDouble() < CatchAFishScore.DOUBLE_POINTS_CHANCE;
        int points = score.onCatch(caught.color, doubled);
        lastHook = tick;
        player.sendMessage("<a>+" + points + " <7>(" + caught.color.getColor() + caught.color.getDisplayName() + " Fish<7>)"
                + (doubled ? " <6><l>DOUBLE POINTS!" : "")
                + (score.getStreak() > 1 ? " <6>Catch Streak: " + score.getStreak() : ""));
        player.playSound(Sound.sound(Key.key("entity.experience_orb.pickup"), Sound.Source.PLAYER, 1f, 1f));
        unhook();
        remove(caught);
    }

    private void unhook() {
        if (bobber != null) bobber.remove();
        bobber = null;
        hooked = null;
    }

    private void remove(Fish swimmer) {
        swimmer.entity.remove();
        fish.remove(swimmer);
    }

    @Override
    protected long score() {
        return score.getScore();
    }

    @Override
    protected void onEnd() {
        unhook();
        List.copyOf(fish).forEach(this::remove);
    }

    @Override
    protected CarnivalGameResult result() {
        return score.result();
    }

    @Override
    protected List<String> summary(CarnivalGameResult result) {
        return List.of(
                "<3><l>Catch a Fish",
                "",
                "<7>Your Score",
                "<a>" + StringUtility.commaify(result.score()),
                "",
                "<7>Fish Caught: <e>" + result.metric(Metric.FISH) + " <8>(" + result.metric(Metric.YELLOW_FISH) + " Yellow)",
                "<7>Best Catch Streak: <6>" + result.metric(Metric.BEST_STREAK));
    }
}
