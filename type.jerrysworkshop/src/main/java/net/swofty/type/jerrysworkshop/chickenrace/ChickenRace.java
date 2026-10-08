package net.swofty.type.jerrysworkshop.chickenrace;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
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
import net.swofty.type.skyblockgeneric.chickenrace.ChickenRaceProgress;
import net.swofty.type.skyblockgeneric.chickenrace.Cluckpoint;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.mission.MissionData;
import net.swofty.type.skyblockgeneric.mission.missions.jerry.MissionReachCluckpointFour;
import net.swofty.type.skyblockgeneric.user.SkyBlockActionBar;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gulliver's Chicken Race. The rider's chicken flies wherever they look; flying through the hoops in
 * order leads to the cluckpoints. Touching the ground, leaving the chicken, losing it or running out
 * of time ends the run, as does reaching a cluckpoint for the first time.
 */
public final class ChickenRace {
    private static final double CRASH_CLEARANCE = 0.2;
    private static final int RENDER_EVERY_TICKS = 4;
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
        if (course.hoops().isEmpty()) {
            player.sendMessage("<c>The Chicken Race course isn't set up.");
            return;
        }
        ensureTicking();
        Instance instance = HypixelConst.getInstanceContainer();
        LivingEntity chicken = new LivingEntity(EntityType.CHICKEN);
        chicken.setNoGravity(true);
        Run run = new Run(player, chicken, course);
        RUNS.put(player.getUuid(), run);
        player.teleport(course.start()).thenRun(() -> chicken.setInstance(instance, course.start()).thenRun(() -> {
            if (RUNS.get(player.getUuid()) != run) {
                chicken.remove();
                return;
            }
            chicken.addPassenger(player);
            run.startedAt = System.currentTimeMillis();
            player.sendMessage("<e><l>CHICKEN RACE!</l> <e>Fly through the hoops - your chicken follows where you look!");
            player.playSound(Sound.sound(Key.key("entity.chicken.egg"), Sound.Source.NEUTRAL, 1f, 1f));
        }));
    }

    public static void quit(SkyBlockPlayer player) {
        Run run = RUNS.remove(player.getUuid());
        if (run != null) run.cleanup(false);
    }

    public static void quitAll() {
        RUNS.values().forEach(run -> run.cleanup(false));
        RUNS.clear();
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

    private static final class Run {
        private final SkyBlockPlayer player;
        private final LivingEntity chicken;
        private final ChickenRaceCourse course;
        private long startedAt;
        private int nextHoop;
        private double bonusSeconds;
        private int ticks;

        private Run(SkyBlockPlayer player, LivingEntity chicken, ChickenRaceCourse course) {
            this.player = player;
            this.chicken = chicken;
            this.course = course;
        }

        private void tick() {
            if (startedAt == 0) return;
            if (player.isRemoved() || !player.isOnline()) {
                quit(player);
                return;
            }
            if (chicken.isRemoved() || chicken.isDead()) {
                end(this, "<c>Your chicken died!");
                return;
            }
            if (player.getVehicle() != chicken || player.inputs().shift()) {
                end(this, "<c>You left your chicken!");
                return;
            }
            long elapsed = System.currentTimeMillis() - startedAt;
            double remaining = course.startingSeconds() + bonusSeconds - elapsed / 1000D;
            if (remaining <= 0) {
                end(this, "<c>You ran out of time!");
                return;
            }

            Pos look = player.getPosition();
            chicken.setView(look.yaw(), 0);
            chicken.setVelocity(look.direction().mul(course.speed()));
            if (touchingGround()) {
                end(this, "<c>Your chicken touched the ground!");
                return;
            }

            ChickenRaceCourse.Hoop hoop = course.hoops().get(nextHoop);
            if (hoop.contains(chicken.getPosition().asVec().add(0, 0.4, 0), course.hoopRadius())) {
                passHoop(hoop, elapsed);
                if (!RUNS.containsKey(player.getUuid())) return;
            }

            if (ticks++ % RENDER_EVERY_TICKS == 0) {
                HoopRenderer.render(player, course, nextHoop);
            }
            SkyBlockActionBar.getFor(player).addReplacement(SkyBlockActionBar.BarSection.DEFENSE,
                    Text.of("<e>Time Left: <a>{}s <7>(<f>{}s<7>)", String.format("%.1f", remaining),
                            String.format("%.1f", elapsed / 1000D)), 5, 5);
        }

        private boolean touchingGround() {
            if (chicken.isOnGround()) return true;
            Pos position = chicken.getPosition();
            Instance instance = chicken.getInstance();
            if (instance == null) return true;
            Block feet = instance.getBlock(position.add(0, -CRASH_CLEARANCE, 0));
            Block body = instance.getBlock(position.add(0, 0.5, 0));
            Block head = instance.getBlock(position.add(0, 1.8, 0));
            return !feet.isAir() || !body.isAir() || head.isSolid();
        }

        private void passHoop(ChickenRaceCourse.Hoop hoop, long elapsed) {
            nextHoop++;
            if (hoop.green()) {
                bonusSeconds += course.greenHoopSeconds();
                player.playSound(Sound.sound(Key.key("entity.experience_orb.pickup"), Sound.Source.PLAYER, 1f, 1.4f));
            } else {
                player.playSound(Sound.sound(Key.key("block.note_block.pling"), Sound.Source.PLAYER, 1f, 1.2f));
            }
            if (hoop.cluckpoint() != null) {
                reachCluckpoint(hoop.cluckpoint(), elapsed);
                return;
            }
            if (nextHoop >= course.hoops().size()) end(this, "<a>You flew the whole course!");
        }

        private void reachCluckpoint(Cluckpoint cluckpoint, long elapsed) {
            String time = String.format("%.2f", elapsed / 1000D);
            switch (Cluckpoint.reach(cluckpoint, ChickenRaceProgress.claimed(player), elapsed,
                    ChickenRaceProgress.hasSpokenToFrosty(player))) {
                case CLAIM -> {
                    end(this, "<e><l>CLUCKPOINT " + cluckpoint.number() + "!</l> <e>Reached in <a>" + time + "s<e>!");
                    claim(cluckpoint);
                }
                case TOO_SLOW -> end(this, "<c>You reached <e>Cluckpoint " + cluckpoint.number() + " <c>in <e>" + time
                        + "s<c>, but Gulliver wants it in <e>\\< " + cluckpoint.getSecondsToBeat() + "s<c>!");
                case NEEDS_FROSTY -> end(this, "<e>[NPC] Gulliver<f>: Bukawk! Puk puk... <a>Frosty<f>?"
                        + "\n<7>Talk to <a>Frosty <7>before racing past <e>Cluckpoint 1<7>.");
                case PASS -> {
                    bonusSeconds += course.cluckpointSeconds();
                    player.sendMessage("<e>Cluckpoint " + cluckpoint.number() + " <7>passed in <a>" + time + "s<7>.");
                    if (nextHoop >= course.hoops().size()) end(this, "<a>You flew the whole course!");
                }
            }
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
            if (returnToGulliver && player.isOnline()) player.teleport(course.finish());
        }
    }
}
