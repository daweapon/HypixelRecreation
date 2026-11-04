package dev.daweapon.type.theend;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.theend.MobZealot;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.region.SkyBlockRegion;
import dev.daweapon.type.theend.dragon.EnderDragonFight;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Ender Dragon page: "After some time has passed, the dragon will summon groups of Zealots. These Zealots are
 * immediately hostile ... The Dragon also makes already existing Zealots hostile." The delay, interval and
 * group sizes are estimates.
 */
public final class DragonZealotSummons {
    public static final int FIRST_SUMMON_SECONDS = 60;
    public static final int SUMMON_INTERVAL_SECONDS = 45;
    public static final int GROUPS = 3;
    public static final int GROUP_SIZE = 3;
    private static final double SCATTER = 4;

    private static int activeSeconds;

    private DragonZealotSummons() {
    }

    public static void start() {
        MinecraftServer.getSchedulerManager().buildTask(DragonZealotSummons::tick)
                .repeat(TaskSchedule.seconds(1)).schedule();
    }

    private static void tick() {
        if (!EnderDragonFight.isActive()) {
            activeSeconds = 0;
            return;
        }
        activeSeconds++;
        if (activeSeconds < FIRST_SUMMON_SECONDS) return;
        if ((activeSeconds - FIRST_SUMMON_SECONDS) % SUMMON_INTERVAL_SECONDS != 0) return;
        Instance instance = HypixelConst.getInstanceContainer();
        if (instance != null) summon(instance);
    }

    private static void summon(Instance instance) {
        List<Player> fighters = new ArrayList<>(instance.getPlayers().stream().filter(DragonZealotSummons::inNest).toList());
        if (fighters.isEmpty()) return;
        Collections.shuffle(fighters);
        for (Player fighter : fighters.subList(0, Math.min(GROUPS, fighters.size()))) {
            Pos near = fighter.getPosition();
            for (int i = 0; i < GROUP_SIZE; i++) {
                double dx = ThreadLocalRandom.current().nextDouble(-SCATTER, SCATTER);
                double dz = ThreadLocalRandom.current().nextDouble(-SCATTER, SCATTER);
                MobZealot zealot = new MobZealot();
                zealot.enrage();
                zealot.setInstance(instance, near.add(dx, 0, dz));
            }
        }
        for (SkyBlockMob mob : new ArrayList<>(SkyBlockMob.getMobs())) {
            if (mob instanceof MobZealot zealot && mob.getInstance() == instance && !mob.isDead()) zealot.enrage();
        }
    }

    private static boolean inNest(Player player) {
        SkyBlockRegion region = SkyBlockRegion.getRegionOfPosition(player.getPosition());
        return region != null && region.getType() == RegionType.DRAGONS_NEST;
    }
}
