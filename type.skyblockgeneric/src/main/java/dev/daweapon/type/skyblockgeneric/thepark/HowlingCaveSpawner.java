package dev.daweapon.type.skyblockgeneric.thepark;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.thepark.MobSoulOfTheAlpha;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class HowlingCaveSpawner {
    private static final List<Slot> SLOTS = new ArrayList<>();

    private HowlingCaveSpawner() {
    }

    public static void start() {
        if (!SLOTS.isEmpty()) return;
        for (HowlingCavePacks.Pack pack : HowlingCavePacks.PACKS) {
            SLOTS.add(new Slot(MobSoulOfTheAlpha::new, pack.alpha()));
            for (HowlingCavePacks.Spirit spirit : pack.spirits()) SLOTS.add(new Slot(spirit.kind()::create, spirit.spot()));
        }
        MinecraftServer.getSchedulerManager().buildTask(HowlingCaveSpawner::tick)
                .repeat(TaskSchedule.seconds(1)).schedule();
    }

    private static void tick() {
        Instance instance = HypixelConst.getInstanceContainer();
        if (instance == null || !playerNearby(instance)) return;
        long now = System.currentTimeMillis();
        for (Slot slot : SLOTS) slot.update(instance, now);
    }

    private static boolean playerNearby(Instance instance) {
        for (Player player : instance.getPlayers()) {
            if (player.getPosition().distance(HowlingCavePacks.CAVE_CENTRE) <= HowlingCavePacks.ACTIVE_RANGE) return true;
        }
        return false;
    }

    private static final class Slot {
        private final Supplier<SkyBlockMob> factory;
        private final Pos spot;
        private SkyBlockMob mob;
        private long dueAt;

        private Slot(Supplier<SkyBlockMob> factory, Pos spot) {
            this.factory = factory;
            this.spot = spot;
        }

        private void update(Instance instance, long now) {
            if (mob != null && (mob.isDead() || mob.isRemoved() || mob.getInstance() != instance)) {
                mob = null;
                dueAt = now + HowlingCavePacks.RESPAWN_MILLIS;
            }
            if (mob != null || now < dueAt) return;
            if (!instance.isChunkLoaded(spot)) instance.loadChunk(spot).join();
            mob = factory.get();
            mob.setInstance(instance, spot);
        }
    }
}
