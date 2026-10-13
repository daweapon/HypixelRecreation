package net.swofty.type.crystalhollows.boss;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.crystalhollows.layout.CrystalHollowsLayout;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crystalhollows.boss.MobBal;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crystalhollows.boss.MobBossCorleone;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.region.SkyBlockRegion;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Keeps Boss Corleone in the Mithril Deposits and Bal in Khazad-dum while a player is in the zone. Both spawn at a
 * layout anchor, falling back to the structure's origin; until the world's structures are placed nothing spawns.
 * Bal returns a minute after he falls (wiki); Corleone's five minutes is an estimate.
 */
public final class CrystalHollowsBossSpawner {
    private static final List<Slot> SLOTS = List.of(
            new Slot(MobBossCorleone::new, "mines_of_divan", "corleone",
                    Set.of(RegionType.MITHRIL_DEPOSITS, RegionType.MINES_OF_DIVAN), Duration.ofMinutes(5)),
            new Slot(MobBal::new, "khazad_dum", "bal", Set.of(RegionType.KHAZAD_DUM), Duration.ofMinutes(1)));
    private static boolean running;

    private CrystalHollowsBossSpawner() {
    }

    public static synchronized void start() {
        if (running) return;
        running = true;
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            Instance instance = HypixelConst.getInstanceContainer();
            if (instance != null) SLOTS.forEach(slot -> slot.tick(instance));
            return TaskSchedule.seconds(1);
        });
    }

    private static final class Slot {
        private final Supplier<SkyBlockMob> factory;
        private final String structure;
        private final String anchor;
        private final Set<RegionType> regions;
        private final Duration respawn;
        private @Nullable SkyBlockMob mob;
        private long respawnAt;

        private Slot(Supplier<SkyBlockMob> factory, String structure, String anchor, Set<RegionType> regions, Duration respawn) {
            this.factory = factory;
            this.structure = structure;
            this.anchor = anchor;
            this.regions = regions;
            this.respawn = respawn;
        }

        private void tick(Instance instance) {
            long now = System.currentTimeMillis();
            if (mob != null) {
                if (!mob.isDead() && !mob.isRemoved()) return;
                mob = null;
                respawnAt = now + respawn.toMillis();
            }
            if (now < respawnAt || !playerInZone(instance)) return;
            Pos spawn = CrystalHollowsLayout.anchor(structure, anchor);
            if (spawn == null) spawn = CrystalHollowsLayout.origin(structure);
            if (spawn == null) return;
            if (!instance.isChunkLoaded(spawn)) instance.loadChunk(spawn).join();
            mob = factory.get();
            mob.setInstance(instance, spawn);
        }

        private boolean playerInZone(Instance instance) {
            return instance.getPlayers().stream().anyMatch(player -> {
                SkyBlockRegion region = SkyBlockRegion.getRegionOfPosition(player.getPosition());
                return region != null && regions.contains(region.getType());
            });
        }
    }
}
