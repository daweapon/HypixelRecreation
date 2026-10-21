package dev.daweapon.type.skyblockgeneric.hunting.creature.trigger;

import dev.daweapon.type.generic.utility.ScheduleUtility;
import dev.daweapon.type.skyblockgeneric.hunting.creature.CreatureSpec;
import dev.daweapon.type.skyblockgeneric.hunting.creature.HuntingCreatureMob;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.region.SkyBlockRegion;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ThreadLocalRandom;

final class AntHills {
    private static final int RESTORE_TICKS = 20 * 30;
    private static final double SCATTER = 2.5;
    private static final Map<HuntingCreatureMob, List<HuntingCreatureMob>> ESCORTS = Collections.synchronizedMap(new WeakHashMap<>());

    private AntHills() {
    }

    static List<HuntingCreatureMob> escortOf(HuntingCreatureMob queen) {
        return List.copyOf(ESCORTS.getOrDefault(queen, List.of()));
    }

    static boolean handle(SkyBlockPlayer player, Point position, Block block) {
        if (!block.compare(Block.POINTED_DRIPSTONE) || player.getInstance() == null) return false;
        SkyBlockRegion region = SkyBlockRegion.getRegionOfPosition(position);
        if (region == null || region.getType() != RegionType.ANTS_CAVE) return false;
        List<CreatureSpec> specs = TriggerSupport.specs("ANT_HILL_BREAK");
        CreatureSpec ant = specs.stream().filter(spec -> spec.spawn().trigger().details().containsKey("min")).findFirst().orElse(null);
        if (ant == null) return false;
        CreatureSpec queen = specs.stream().filter(spec -> spec != ant).findFirst().orElse(null);
        Instance instance = player.getInstance();
        Map<Point, Block> stack = collectStack(instance, position);
        stack.keySet().forEach(point -> instance.setBlock(point, Block.AIR));
        ScheduleUtility.delay(() -> stack.forEach(instance::setBlock), RESTORE_TICKS);
        TriggerSupport.tell(player, ant, "hill_destroyed");
        release(player, instance, position.asPos().add(0.5, 0, 0.5), ant, queen);
        return true;
    }

    private static void release(SkyBlockPlayer player, Instance instance, Pos center, CreatureSpec ant, CreatureSpec queen) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int count = TriggerMath.antCount(number(ant, "min", 2), number(ant, "max", 4), random);
        List<HuntingCreatureMob> escort = new ArrayList<>();
        boolean queenRolled = false;
        double chancePerAnt = queen == null ? 0 : queen.spawn().trigger().details().get("chancePerAnt") instanceof Number n ? n.doubleValue() : 0;
        for (int i = 0; i < count; i++) {
            HuntingCreatureMob spawned = TriggerSupport.spawn(ant, instance, TriggerSupport.scatter(instance, center, ant, SCATTER));
            if (spawned != null) escort.add(spawned);
            if (!queenRolled && queen != null && TriggerSupport.roll(player.getUuid(), queen, chancePerAnt)) queenRolled = true;
        }
        if (!queenRolled) return;
        HuntingCreatureMob queenMob = TriggerSupport.spawn(queen, instance, TriggerSupport.scatter(instance, center, queen, SCATTER));
        if (queenMob == null) return;
        ESCORTS.put(queenMob, escort);
        TriggerSupport.tell(player, queen, "hill_destroyed");
    }

    private static int number(CreatureSpec spec, String key, int fallback) {
        return spec.spawn().trigger().details().get(key) instanceof Number n ? n.intValue() : fallback;
    }

    private static Map<Point, Block> collectStack(Instance instance, Point origin) {
        Map<Point, Block> stack = new LinkedHashMap<>();
        for (int step : new int[]{1, -1}) {
            for (int y = origin.blockY(); Math.abs(y - origin.blockY()) < 8; y += step) {
                Point point = origin.withY(y);
                Block block = instance.getBlock(point);
                if (!block.compare(Block.POINTED_DRIPSTONE)) break;
                stack.put(point, block);
            }
        }
        return stack;
    }
}
