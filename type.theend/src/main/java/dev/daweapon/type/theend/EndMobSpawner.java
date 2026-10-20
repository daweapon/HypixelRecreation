package dev.daweapon.type.theend;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.theend.MobEndermanBottom;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.theend.MobEndermanHigh;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.theend.MobEndermanLow;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.theend.MobEndermanMid;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.theend.MobEndermiteHigh;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.theend.MobEndermiteLow;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.theend.MobObsidianDefender;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.theend.MobSeer;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.theend.MobVoidlingExtremist;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.theend.MobVoidlingFanatic;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public final class EndMobSpawner {
    public static final int CHECK_SECONDS = 5;
    public static final double PLAYER_RANGE = 110;
    public static final double MIN_DISTANCE = 12;

    public static final List<Pos> LOW = List.of(
            new Pos(-494.5, 24, -292.5), new Pos(-542.5, 21, -262.5), new Pos(-560.5, 12, -286.5), new Pos(-542.5, 21, -310.5),
            new Pos(-482.5, 18, -292.5), new Pos(-494.5, 19, -220.5), new Pos(-560.5, 24, -226.5), new Pos(-482.5, 19, -256.5),
            new Pos(-536.5, 8, -298.5), new Pos(-560.5, 19, -244.5));
    public static final List<Pos> MID = List.of(
            new Pos(-512.5, 71, -292.5), new Pos(-536.5, 58, -220.5), new Pos(-548.5, 41, -310.5), new Pos(-500.5, 62, -220.5),
            new Pos(-560.5, 45, -202.5), new Pos(-536.5, 74, -328.5), new Pos(-482.5, 54, -280.5), new Pos(-506.5, 53, -238.5),
            new Pos(-554.5, 66, -262.5), new Pos(-560.5, 72, -286.5));
    public static final List<Pos> HIGH = List.of(
            new Pos(-488.5, 98, -292.5), new Pos(-506.5, 101, -268.5), new Pos(-512.5, 105, -202.5), new Pos(-560.5, 84, -226.5),
            new Pos(-542.5, 102, -286.5), new Pos(-518.5, 100, -292.5), new Pos(-560.5, 99, -292.5), new Pos(-542.5, 103, -250.5),
            new Pos(-524.5, 102, -280.5), new Pos(-554.5, 76, -310.5));
    public static final List<Pos> NEST = List.of(
            new Pos(-642.5, 5, -328.5), new Pos(-626.5, 9, -296.5), new Pos(-674.5, 5, -328.5), new Pos(-658.5, 5, -264.5),
            new Pos(-754.5, 12, -304.5), new Pos(-626.5, 11, -280.5), new Pos(-714.5, 5, -288.5), new Pos(-730.5, 5, -264.5),
            new Pos(-610.5, 11, -248.5), new Pos(-698.5, 9, -296.5), new Pos(-650.5, 5, -272.5), new Pos(-690.5, 5, -328.5),
            new Pos(-722.5, 8, -344.5), new Pos(-690.5, 15, -376.5), new Pos(-690.5, 10, -312.5), new Pos(-682.5, 5, -280.5));
    public static final List<Pos> SEPULTURE = List.of(
            new Pos(-592.5, 11, -338.5), new Pos(-572.5, 8, -314.5), new Pos(-588.5, 26, -346.5), new Pos(-588.5, 26, -326.5), new Pos(-584.5, 10, -310.5), new Pos(-580.5, 12, -346.5), new Pos(-580.5, 26, -334.5), new Pos(-588.5, 10, -294.5));

    private record Plan(Class<? extends SkyBlockMob> type, Supplier<SkyBlockMob> factory, List<Pos> spots, int minimum) {
    }

    private static final List<Plan> PLANS = List.of(
            new Plan(MobEndermanHigh.class, MobEndermanHigh::new, LOW, 3),
            new Plan(MobEndermanMid.class, MobEndermanMid::new, MID, 2),
            new Plan(MobEndermanLow.class, MobEndermanLow::new, HIGH, 2),
            new Plan(MobEndermanBottom.class, () -> new MobEndermanBottom(true), NEST, 5),
            new Plan(MobEndermiteLow.class, MobEndermiteLow::new, LOW, 2),
            new Plan(MobSeer.class, MobSeer::new, NEST, 4),
            new Plan(MobObsidianDefender.class, MobObsidianDefender::new, NEST, 4),
            new Plan(MobEndermiteHigh.class, MobEndermiteHigh::new, NEST, 4),
            new Plan(MobVoidlingFanatic.class, MobVoidlingFanatic::new, SEPULTURE, 3),
            new Plan(MobVoidlingExtremist.class, MobVoidlingExtremist::new, SEPULTURE, 1));

    private EndMobSpawner() {
    }

    public static void start() {
        MinecraftServer.getSchedulerManager().buildTask(EndMobSpawner::tick)
                .repeat(TaskSchedule.seconds(CHECK_SECONDS)).schedule();
    }

    private static void tick() {
        Instance instance = HypixelConst.getInstanceContainer();
        if (instance == null || instance.getPlayers().isEmpty()) return;
        for (Plan plan : PLANS) {
            long alive = SkyBlockMob.getMobs().stream()
                    .filter(mob -> mob.getClass() == plan.type() && mob.getInstance() == instance && !mob.isDead())
                    .count();
            for (long i = alive; i < plan.minimum(); i++) spawn(instance, plan);
        }
    }

    private static void spawn(Instance instance, Plan plan) {
        List<Pos> usable = new ArrayList<>();
        for (Pos spot : plan.spots()) {
            double nearest = nearestPlayer(instance, spot);
            if (nearest <= PLAYER_RANGE && nearest >= MIN_DISTANCE) usable.add(spot);
        }
        if (usable.isEmpty()) return;
        Pos spot = usable.get(ThreadLocalRandom.current().nextInt(usable.size()));
        if (!instance.isChunkLoaded(spot)) instance.loadChunk(spot).join();
        plan.factory().get().setInstance(instance, spot);
    }

    private static double nearestPlayer(Instance instance, Pos spot) {
        double nearest = Double.MAX_VALUE;
        for (Player player : instance.getPlayers()) nearest = Math.min(nearest, player.getPosition().distance(spot));
        return nearest;
    }
}
