package dev.daweapon.type.skyblockgeneric.technoblade;

import dev.daweapon.commons.ServerType;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.timer.TaskSchedule;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public final class ThunderSpawner {
    public static final int INTERVAL_SECONDS = 20;
    public static final double CREEPER_CHANCE = 0.5;
    public static final double HORSE_CHANCE = 0.1;
    public static final int MAX_CREEPERS = 6;
    public static final int MAX_HORSES = 1;
    private static final double SPREAD = 6;

    private static final List<CrimsonIsleMob> CREEPERS = new ArrayList<>();
    private static final List<CrimsonIsleMob> HORSES = new ArrayList<>();

    private ThunderSpawner() {
    }

    public static void initialize() {
        MinecraftServer.getSchedulerManager().buildTask(ThunderSpawner::tick)
                .repeat(TaskSchedule.seconds(INTERVAL_SECONDS)).schedule();
    }

    private static void tick() {
        prune(CREEPERS);
        prune(HORSES);
        if (!AncientPigEffects.active(AncientPigEffect.THUNDER)
                || HypixelConst.getTypeLoader().getType() == ServerType.SKYBLOCK_ISLAND) {
            clear(CREEPERS);
            clear(HORSES);
            return;
        }
        ThreadLocalRandom random = ThreadLocalRandom.current();
        if (CREEPERS.size() < MAX_CREEPERS && random.nextDouble() < CREEPER_CHANCE) spawn(CREEPERS, MobChargedCreeper::new);
        if (HORSES.size() < MAX_HORSES && random.nextDouble() < HORSE_CHANCE) spawn(HORSES, MobThunderSkeletonHorse::new);
    }

    private static void spawn(List<CrimsonIsleMob> tracked, Supplier<CrimsonIsleMob> factory) {
        List<SkyBlockMob> anchors = new ArrayList<>();
        for (SkyBlockMob mob : new ArrayList<>(SkyBlockMob.getMobs())) {
            if (mob.isDead() || mob.isRemoved() || mob.getInstance() == null) continue;
            if (mob instanceof MobChargedCreeper || mob instanceof MobThunderSkeletonHorse) continue;
            anchors.add(mob);
        }
        if (anchors.isEmpty()) return;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        SkyBlockMob anchor = anchors.get(random.nextInt(anchors.size()));
        Pos at = anchor.getPosition().add((random.nextDouble() - 0.5) * SPREAD, 0, (random.nextDouble() - 0.5) * SPREAD);
        CrimsonIsleMob mob = factory.get();
        mob.setInstance(anchor.getInstance(), at);
        tracked.add(mob);
    }

    private static void prune(List<CrimsonIsleMob> tracked) {
        tracked.removeIf(mob -> mob.isDead() || mob.isRemoved());
    }

    private static void clear(List<CrimsonIsleMob> tracked) {
        tracked.forEach(mob -> {
            if (!mob.isRemoved()) mob.remove();
        });
        tracked.clear();
    }
}
