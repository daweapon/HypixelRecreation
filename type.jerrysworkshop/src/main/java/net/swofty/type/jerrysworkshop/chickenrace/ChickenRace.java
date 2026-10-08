package net.swofty.type.jerrysworkshop.chickenrace;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.title.Title;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.skyblockgeneric.chickenrace.ChickenFlight;
import net.swofty.type.skyblockgeneric.chickenrace.ChickenRaceProgress;
import net.swofty.type.skyblockgeneric.chickenrace.Cluckpoint;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.mission.MissionData;
import net.swofty.type.skyblockgeneric.mission.missions.jerry.MissionReachCluckpointFour;
import net.swofty.type.skyblockgeneric.user.SkyBlockActionBar;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gulliver's Chicken Race. The chicken flies forwards wherever the rider faces, falls under gravity
 * and flaps upwards on JUMP. Flying through the hoops in order leads to the cluckpoints (the green time
 * hoops and the finish), each of which tops up the countdown. Reaching an unclaimed cluckpoint ends the
 * run with its reward; touching a block, leaving the chicken or running out of time cancels it.
 */
public final class ChickenRace {
    private static final String TITLE = "<6><l>CHICKEN RACING</l>";
    private static final int RENDER_EVERY_TICKS = 3;
    private static final double RIDER_HEIGHT = 1.0;
    private static final Map<UUID, Run> RUNS = new ConcurrentHashMap<>();
    private static boolean ticking;

    private ChickenRace() {
    }

    public static boolean isRacing(SkyBlockPlayer player) {
        return RUNS.containsKey(player.getUuid());
    }

    public static synchronized void start(SkyBlockPlayer player) {
        if (isRacing(player) || player.getVehicle() != null) return;
        ChickenRaceCourse course = ChickenRaceCourse.get();
        if (course.hoops().isEmpty() || course.finish() == null) {
            player.sendMessage("<c>The Chicken Race course isn't set up.");
            return;
        }
        ensureTicking();
        Instance instance = HypixelConst.getInstanceContainer();
        LivingEntity chicken = new LivingEntity(EntityType.CHICKEN);
        chicken.setNoGravity(true);
        chicken.setInvulnerable(true);
        Run run = new Run(player, chicken, course);
        RUNS.put(player.getUuid(), run);
        player.teleport(course.start()).thenRun(() -> chicken.setInstance(instance, course.start()).thenRun(() -> {
            if (RUNS.get(player.getUuid()) != run) {
                chicken.remove();
                return;
            }
            chicken.addPassenger(player);
            run.begin();
        }));
    }

    public static void jump(SkyBlockPlayer player) {
        Run run = RUNS.get(player.getUuid());
        if (run != null) run.jumpPressed = true;
    }

    public static void quit(SkyBlockPlayer player) {
        Run run = RUNS.remove(player.getUuid());
        if (run != null) run.cleanup(false);
    }

    public static void quitAll() {
        RUNS.values().forEach(run -> run.cleanup(false));
        RUNS.clear();
    }

    static String clock(long millis) {
        long clamped = Math.max(0, millis);
        return String.format("%02d:%02d.%03d", clamped / 60_000, clamped / 1000 % 60, clamped % 1000);
    }

    private static void ensureTicking() {
        if (ticking) return;
        ticking = true;
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            RUNS.values().forEach(Run::tick);
            return TaskSchedule.tick(1);
        });
        MinecraftServer.getSchedulerManager().buildShutdownTask(ChickenRace::quitAll);
    }

    private static void end(Run run, String message) {
        if (RUNS.remove(run.player.getUuid(), run)) {
            run.cleanup(true);
            run.player.sendMessage(message);
        }
    }

    private static void cancel(Run run, String reason) {
        end(run, TITLE + " <c>Race cancelled! " + reason);
        run.player.playSound(Sound.sound(Key.key("block.note_block.bass"), Sound.Source.PLAYER, 1f, 0.6f));
    }

    private static final class Run {
        private final SkyBlockPlayer player;
        private final LivingEntity chicken;
        private final ChickenRaceCourse course;
        private final List<ChickenRaceCourse.Hoop> gates;
        private final ChickenFlight flight;
        private final LowTimeTint tint;
        private volatile boolean jumpPressed;
        private long startedAt;
        private Vec lastPosition;
        private int nextGate;
        private int cluckpointsPassed;
        private int ticks;

        private Run(SkyBlockPlayer player, LivingEntity chicken, ChickenRaceCourse course) {
            this.player = player;
            this.chicken = chicken;
            this.course = course;
            this.gates = course.gates();
            this.flight = new ChickenFlight(course.physics());
            this.tint = new LowTimeTint(player, course.tintStartSeconds(), course.tintFullSeconds());
        }

        private void begin() {
            lastPosition = riderPoint();
            flight.flap();
            startedAt = System.currentTimeMillis();
            player.showTitle(Text.of(TITLE), Text.of("<e>Start"),
                    Title.Times.times(Duration.ZERO, Duration.ofMillis(1500), Duration.ofMillis(500)));
            player.sendMessage(TITLE + " <e>Race started! Good cluck!");
            player.playSound(Sound.sound(Key.key("entity.chicken.egg"), Sound.Source.NEUTRAL, 1f, 1f));
        }

        private void tick() {
            if (startedAt == 0) return;
            if (player.isRemoved() || !player.isOnline()) {
                quit(player);
                return;
            }
            if (chicken.isRemoved() || chicken.isDead() || player.getVehicle() != chicken || player.inputs().shift()) {
                cancel(this, "You abandoned your Chicken!");
                return;
            }
            long elapsed = System.currentTimeMillis() - startedAt;

            Vec position = riderPoint();
            while (nextGate < gates.size() && gates.get(nextGate).crossed(lastPosition, position)) {
                passGate(gates.get(nextGate++), elapsed);
                if (!isRacing(player)) return;
            }
            lastPosition = position;

            long remaining = Cluckpoint.remainingMillis(cluckpointsPassed, elapsed);
            if (remaining <= 0) {
                cancel(this, "You ran out of time!");
                return;
            }
            if (ticks > course.launchGraceTicks() && touchingBlock()) {
                cancel(this, "Your Chicken touched the ground!");
                return;
            }

            boolean jumping = jumpPressed || player.inputs().jump();
            jumpPressed = false;
            double rise = flight.tick(jumping);
            float yaw = player.getPosition().yaw();
            Vec forward = new Vec(-Math.sin(Math.toRadians(yaw)), 0, Math.cos(Math.toRadians(yaw)))
                    .mul(course.physics().blocksPerTick());
            chicken.setView(yaw, 0);
            chicken.setVelocity(forward.withY(rise).mul(MinecraftServer.TICK_PER_SECOND));

            if (ticks++ % RENDER_EVERY_TICKS == 0) HoopRenderer.render(player, gates, nextGate);
            showHud(remaining);
            tint.update(remaining / 1000D);
        }

        private Vec riderPoint() {
            return chicken.getPosition().asVec().add(0, RIDER_HEIGHT, 0);
        }

        private boolean touchingBlock() {
            if (chicken.isOnGround()) return true;
            Instance instance = chicken.getInstance();
            if (instance == null) return true;
            Pos position = chicken.getPosition();
            Block feet = instance.getBlock(position.add(0, -0.2, 0));
            Block body = instance.getBlock(position.add(0, 0.5, 0));
            Block head = instance.getBlock(position.add(0, 1.8, 0));
            return !feet.isAir() || !body.isAir() || head.isSolid();
        }

        private void showHud(long remaining) {
            int hoopsPassed = Math.min(nextGate, course.hoops().size());
            Text bar = Text.read(TITLE + " <e>" + clock(remaining) + " <b>" + hoopsPassed + "/" + course.hoops().size()
                    + " " + (flight.flapReady() ? "<a>" : "<7>") + "JUMP");
            SkyBlockActionBar actionBar = SkyBlockActionBar.getFor(player);
            actionBar.addReplacement(SkyBlockActionBar.BarSection.HEALTH, bar, 1, 100);
            actionBar.addReplacement(SkyBlockActionBar.BarSection.DEFENSE, Text.empty(), 1, 100);
            actionBar.addReplacement(SkyBlockActionBar.BarSection.MANA, Text.empty(), 1, 100);
        }

        private void passGate(ChickenRaceCourse.Hoop gate, long elapsed) {
            if (gate.cluckpoint() == null) {
                player.playSound(Sound.sound(Key.key("entity.experience_orb.pickup"), Sound.Source.PLAYER, 1f, 1.2f));
                return;
            }
            Cluckpoint cluckpoint = gate.cluckpoint();
            switch (Cluckpoint.reach(cluckpoint, ChickenRaceProgress.claimed(player), elapsed,
                    ChickenRaceProgress.hasSpokenToFrosty(player))) {
                case CLAIM -> {
                    end(this, cluckpoint == Cluckpoint.FOUR ? finishedMessage(elapsed)
                            : TITLE + " <e>You reached <a>Cluckpoint " + cluckpoint.number() + " <e>in " + clock(elapsed) + "!");
                    claim(cluckpoint);
                }
                case TOO_SLOW -> cancel(this, "You reached <e>Cluckpoint " + cluckpoint.number()
                        + " <c>in <e>" + clock(elapsed) + "<c>, but Gulliver wants it in under <e>"
                        + cluckpoint.getSecondsToBeat() + "s<c>!");
                case NEEDS_FROSTY -> end(this, "<e>[NPC] Gulliver<f>: Bukawk! Puk puk... <a>Frosty<f>?"
                        + "\n<7>Talk to <a>Frosty <7>before racing past <e>Cluckpoint 1<7>.");
                case PASS -> {
                    cluckpointsPassed++;
                    if (cluckpoint == Cluckpoint.FOUR) {
                        end(this, finishedMessage(elapsed));
                        player.playSound(Sound.sound(Key.key("ui.toast.challenge_complete"), Sound.Source.PLAYER, 1f, 1f));
                        return;
                    }
                    player.sendMessage("<a>+" + cluckpoint.bonusSeconds() + " Seconds!");
                    player.playSound(Sound.sound(Key.key("entity.player.levelup"), Sound.Source.PLAYER, 1f, 1.6f));
                }
            }
        }

        private String finishedMessage(long elapsed) {
            return TITLE + " <e>Race finished in " + clock(elapsed) + "!";
        }

        private void claim(Cluckpoint cluckpoint) {
            ChickenRaceProgress.setClaimed(player, cluckpoint.number());
            player.addAndUpdateItem(new SkyBlockItem(cluckpoint.getReward()));
            player.sendMessage("<e>[NPC] Gulliver<f>: Cluck cluck! Bacawk! " + cluckpoint.getRewardMarkup() + "<f> bakok.");
            player.playSound(Sound.sound(Key.key("ui.toast.challenge_complete"), Sound.Source.PLAYER, 1f, 1f));
            MissionData missions = player.getMissionData();
            if (cluckpoint == Cluckpoint.FOUR && missions.isCurrentlyActive(MissionReachCluckpointFour.class)) {
                missions.endMission(MissionReachCluckpointFour.class);
            }
        }

        private void cleanup(boolean returnToGulliver) {
            if (chicken.getPassengers().contains(player)) chicken.removePassenger(player);
            chicken.remove();
            tint.clear();
            if (returnToGulliver && player.isOnline()) player.teleport(course.returnTo());
        }
    }
}
