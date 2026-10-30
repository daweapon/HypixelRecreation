package dev.daweapon.type.skyblockgeneric.glacite.corpse.mob;

import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.glacite.MobGlaciteBowman;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.glacite.MobGlaciteCaver;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.glacite.MobGlaciteMage;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.glacite.MobGlaciteMutt;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.glacite.MobLittlefoot;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public final class ShaftMobSpawns {
    public static final double LITTLEFOOT_REPLACEMENT_PERCENT = 1;
    public static final int MOBS_PER_SPOT = 2;
    private static final double SPREAD = 5;
    private static final List<Supplier<? extends SkyBlockMob>> REGULAR = List.of(
            MobGlaciteBowman::new, MobGlaciteCaver::new, MobGlaciteMage::new, MobGlaciteMutt::new);

    private ShaftMobSpawns() {
    }

    public static boolean isDen(String layoutCode) {
        return layoutCode.startsWith("LITT");
    }

    public static boolean replacesWithLittlefoot(String layoutCode, double roll) {
        return isDen(layoutCode) || roll * 100 < LITTLEFOOT_REPLACEMENT_PERCENT;
    }

    public static void populate(Instance instance, String layoutCode, List<Pos> spots) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        boolean denLittlefootPlaced = false;
        for (Pos spot : spots) {
            for (int i = 0; i < MOBS_PER_SPOT; i++) {
                Pos at = spot.add((random.nextDouble() - 0.5) * 2 * SPREAD, 0, (random.nextDouble() - 0.5) * 2 * SPREAD);
                SkyBlockMob mob;
                if (isDen(layoutCode) && !denLittlefootPlaced) {
                    denLittlefootPlaced = true;
                    mob = new MobLittlefoot();
                } else if (!isDen(layoutCode) && replacesWithLittlefoot(layoutCode, random.nextDouble())) {
                    mob = new MobLittlefoot();
                } else {
                    mob = REGULAR.get(random.nextInt(REGULAR.size())).get();
                }
                mob.setInstance(instance, at);
            }
        }
    }
}
