package dev.daweapon.type.theend.dragon;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.component.DataComponents;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import dev.daweapon.type.generic.entity.drop.ItemDrops;
import dev.daweapon.type.skyblockgeneric.entity.DroppedItemEntityImpl;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.theend.ZealotWaves;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class DragonLootSite {
    private static final double SCATTER_RADIUS = 1.6;

    private record Saved(Pos at, Block block) {
    }

    private static final List<Saved> REPLACED = new ArrayList<>();
    private static Pos beacon;

    private DragonLootSite() {
    }

    public static Pos nearestFloor(Pos deathAt) {
        return ZealotWaves.SPOTS.stream()
                .min(Comparator.comparingDouble(spot -> flatDistanceSquared(spot, deathAt)))
                .orElse(ZealotWaves.SPOTS.getFirst());
    }

    private static double flatDistanceSquared(Pos a, Pos b) {
        double dx = a.x() - b.x();
        double dz = a.z() - b.z();
        return dx * dx + dz * dz;
    }

    public static synchronized void build(Instance instance, Pos spot) {
        restore(instance);
        int x = spot.blockX();
        int y = spot.blockY();
        int z = spot.blockZ();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                replace(instance, new Pos(x + dx, y - 1, z + dz), Block.IRON_BLOCK);
            }
        }
        replace(instance, new Pos(x, y, z), Block.BEACON.withNbt(CompoundBinaryTag.builder().putInt("Levels", 1).build()));
        beacon = new Pos(x + 0.5, y + 1, z + 0.5);
    }

    private static void replace(Instance instance, Pos at, Block block) {
        if (!instance.isChunkLoaded(at)) instance.loadChunk(at).join();
        REPLACED.add(new Saved(at, instance.getBlock(at)));
        instance.setBlock(at, block);
    }

    public static synchronized void restore(Instance instance) {
        for (int i = REPLACED.size() - 1; i >= 0; i--) instance.setBlock(REPLACED.get(i).at(), REPLACED.get(i).block());
        REPLACED.clear();
        beacon = null;
    }

    public static synchronized void drop(Instance instance, SkyBlockPlayer player, SkyBlockItem item) {
        if (beacon == null) return;
        double angle = ThreadLocalRandom.current().nextDouble(Math.PI * 2);
        double radius = ThreadLocalRandom.current().nextDouble(0.4, SCATTER_RADIUS);
        Pos at = beacon.add(Math.cos(angle) * radius, 0.4, Math.sin(angle) * radius);
        DroppedItemEntityImpl entity = new DroppedItemEntityImpl(item, player);
        entity.set(DataComponents.CUSTOM_NAME, item.getDisplayNameText().asComponent());
        entity.setCustomNameVisible(true);
        ItemDrops.dropAt(entity, instance, at);
    }
}
