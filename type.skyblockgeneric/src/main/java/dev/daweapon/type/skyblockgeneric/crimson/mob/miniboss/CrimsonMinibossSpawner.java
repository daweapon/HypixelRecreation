package dev.daweapon.type.skyblockgeneric.crimson.mob.miniboss;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobAshfang;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobBarbarianDukeX;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobBladesoul;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobMageOutlaw;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobMagmaBoss;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Keeps each miniboss and the Magma Boss at its spawn, bringing it back its respawn time after it dies.
 */
public final class CrimsonMinibossSpawner {
    private static final Map<CrimsonMiniboss, Supplier<CrimsonMinibossMob>> FACTORIES = Map.of(
            CrimsonMiniboss.BLADESOUL, MobBladesoul::new,
            CrimsonMiniboss.MAGE_OUTLAW, MobMageOutlaw::new,
            CrimsonMiniboss.BARBARIAN_DUKE_X, MobBarbarianDukeX::new,
            CrimsonMiniboss.ASHFANG, MobAshfang::new,
            CrimsonMiniboss.MAGMA_BOSS, MobMagmaBoss::new);
    private static final Map<CrimsonMiniboss, Slot> SLOTS = new EnumMap<>(CrimsonMiniboss.class);
    private static boolean running;

    private CrimsonMinibossSpawner() {
    }

    public static synchronized void start() {
        if (running) return;
        running = true;
        for (CrimsonMiniboss miniboss : CrimsonMiniboss.values()) SLOTS.put(miniboss, new Slot(miniboss));
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            Instance instance = HypixelConst.getInstanceContainer();
            if (instance != null) SLOTS.values().forEach(slot -> slot.tick(instance));
            return TaskSchedule.seconds(1);
        });
    }

    public static @Nullable CrimsonMinibossMob alive(CrimsonMiniboss miniboss) {
        Slot slot = SLOTS.get(miniboss);
        return slot == null ? null : slot.mob;
    }

    public static long millisUntilRespawn(CrimsonMiniboss miniboss) {
        Slot slot = SLOTS.get(miniboss);
        return slot == null ? 0 : Math.max(0, slot.respawnAt - System.currentTimeMillis());
    }

    private static final class Slot {
        private final CrimsonMiniboss miniboss;
        private @Nullable CrimsonMinibossMob mob;
        private long respawnAt;

        private Slot(CrimsonMiniboss miniboss) {
            this.miniboss = miniboss;
        }

        private void tick(Instance instance) {
            long now = System.currentTimeMillis();
            if (mob != null) {
                if (!mob.isDead() && !mob.isRemoved()) return;
                mob = null;
                respawnAt = now + miniboss.getRespawn().toMillis();
            }
            if (now < respawnAt) return;
            if (!instance.isChunkLoaded(miniboss.getSpawn())) instance.loadChunk(miniboss.getSpawn()).join();
            mob = FACTORIES.get(miniboss).get();
            mob.setInstance(instance, miniboss.getSpawn());
        }
    }
}
