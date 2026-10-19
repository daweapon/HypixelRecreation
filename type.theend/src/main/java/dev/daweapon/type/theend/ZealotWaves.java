package dev.daweapon.type.theend;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.theend.MobZealot;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.region.SkyBlockRegion;
import dev.daweapon.type.theend.dragon.EnderDragonFight;

import java.util.ArrayList;
import java.util.List;

public final class ZealotWaves {
    public static final int WAVE_SECONDS = 11;
    public static final int MAX_PER_SPOT = 2;

    public static final List<Pos> SPOTS = List.of(
            new Pos(-658.5, 5, -276.5), new Pos(-659.5, 5, -271.5), new Pos(-665.5, 8, -269.5), new Pos(-667.5, 6, -263.5),
            new Pos(-672.5, 6, -262.5), new Pos(-677.5, 6, -263.5), new Pos(-682.5, 5, -266.5), new Pos(-685.5, 5, -271.5),
            new Pos(-686.5, 5, -276.5), new Pos(-685.5, 5, -281.5), new Pos(-682.5, 5, -286.5), new Pos(-677.5, 6, -289.5),
            new Pos(-672.5, 6, -290.5), new Pos(-667.5, 6, -289.5), new Pos(-665.5, 8, -283.5), new Pos(-659.5, 5, -281.5));

    private static final double SPOT_RADIUS = 6;
    private static final List<MobZealot> SPAWNED = new ArrayList<>();

    private ZealotWaves() {
    }

    public static void start() {
        MinecraftServer.getSchedulerManager().buildTask(ZealotWaves::wave)
                .repeat(TaskSchedule.seconds(WAVE_SECONDS)).schedule();
    }

    private static void wave() {
        Instance instance = HypixelConst.getInstanceContainer();
        if (instance == null || EnderDragonFight.eggRespawning() || !playersInNest(instance)) return;
        SPAWNED.removeIf(mob -> mob.isRemoved() || mob.isDead());
        for (Pos spot : SPOTS) {
            long here = SPAWNED.stream().filter(mob -> mob.getPosition().distance(spot) <= SPOT_RADIUS).count();
            for (long i = here; i < MAX_PER_SPOT; i++) {
                MobZealot zealot = new MobZealot();
                zealot.setInstance(instance, spot);
                SPAWNED.add(zealot);
            }
        }
    }

    private static boolean playersInNest(Instance instance) {
        return instance.getPlayers().stream().anyMatch(player -> {
            SkyBlockRegion region = SkyBlockRegion.getRegionOfPosition(player.getPosition());
            return region != null && region.getType() == RegionType.DRAGONS_NEST;
        });
    }
}
