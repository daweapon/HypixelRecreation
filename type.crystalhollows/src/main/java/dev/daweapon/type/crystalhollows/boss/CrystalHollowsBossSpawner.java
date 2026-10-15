package dev.daweapon.type.crystalhollows.boss;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.instance.block.Block;
import dev.daweapon.type.crystalhollows.layout.CrystalHollowsLayout;
import dev.daweapon.type.crystalhollows.layout.StructureSpawnPicker;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crystalhollows.boss.MobBal;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crystalhollows.boss.MobBossCorleone;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.region.SkyBlockRegion;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Keeps Boss Corleone in the Mithril Deposits and Bal in Khazad-dum while a player is in the zone. Bal spawns at a
 * layout anchor, falling back to the structure's origin; Corleone spawns on a standable spot inside a randomly chosen
 * placed structure that allows mobs. Until the world's structures are placed nothing spawns.
 * Bal returns a minute after he falls (wiki); Corleone's five minutes is an estimate.
 */
public final class CrystalHollowsBossSpawner {
    private static final List<Slot> SLOTS = List.of(
            new Slot(MobBossCorleone::new, CrystalHollowsBossSpawner::randomStructureSpot,
                    Set.of(RegionType.MITHRIL_DEPOSITS), Duration.ofMinutes(5)),
            new Slot(MobBal::new, instance -> anchorSpot("khazad_dum", "bal"), Set.of(RegionType.KHAZAD_DUM), Duration.ofMinutes(1)));
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

    private static @Nullable Pos anchorSpot(String structure, String anchor) {
        Pos spawn = CrystalHollowsLayout.anchor(structure, anchor);
        return spawn != null ? spawn : CrystalHollowsLayout.origin(structure);
    }

    private static @Nullable Pos randomStructureSpot(Instance instance) {
        List<StructureSpawnPicker.Box> boxes = StructureSpawnPicker.eligibleBoxes(CrystalHollowsLayout.placements().values());
        return StructureSpawnPicker.pick(boxes, new InstanceTerrain(instance), ThreadLocalRandom.current()).orElse(null);
    }

    private record InstanceTerrain(Instance instance) implements StructureSpawnPicker.Terrain {
        @Override
        public boolean isSolid(int x, int y, int z) {
            return block(x, y, z).isSolid();
        }

        @Override
        public boolean isAir(int x, int y, int z) {
            return block(x, y, z).isAir();
        }

        private Block block(int x, int y, int z) {
            if (!instance.isChunkLoaded(x >> 4, z >> 4)) instance.loadChunk(x >> 4, z >> 4).join();
            return instance.getBlock(x, y, z);
        }
    }

    private static final class Slot {
        private final Supplier<SkyBlockMob> factory;
        private final Function<Instance, Pos> spot;
        private final Set<RegionType> regions;
        private final Duration respawn;
        private @Nullable SkyBlockMob mob;
        private long respawnAt;

        private Slot(Supplier<SkyBlockMob> factory, Function<Instance, Pos> spot, Set<RegionType> regions, Duration respawn) {
            this.factory = factory;
            this.spot = spot;
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
            Pos spawn = spot.apply(instance);
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
