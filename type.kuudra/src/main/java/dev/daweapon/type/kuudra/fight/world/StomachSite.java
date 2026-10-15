package dev.daweapon.type.kuudra.fight.world;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraArena;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The stomach's pods in the world: a health hologram over each, and the pod blocks that fall away when it breaks
 * (the Burning capture removed the pod's magenta terracotta, glass and lava core) and grow back afterwards.
 */
public final class StomachSite {
    private static final Set<Block> POD_BLOCKS = Set.of(Block.MAGENTA_TERRACOTTA, Block.MAGENTA_STAINED_GLASS,
            Block.PINK_TERRACOTTA, Block.LAVA);
    private static final Set<Block> POD_SURFACE = Set.of(Block.MAGENTA_TERRACOTTA, Block.MAGENTA_STAINED_GLASS,
            Block.MAGENTA_STAINED_GLASS_PANE, Block.PINK_TERRACOTTA);

    private record Spot(int x, int y, int z) {
    }

    private final Instance instance;
    private final KuudraArena.Stomach stomach;
    private final Map<String, Entity> labels = new HashMap<>();
    private final Map<String, Map<Spot, Block>> broken = new HashMap<>();

    public StomachSite(Instance instance, KuudraArena.Stomach stomach) {
        this.instance = instance;
        this.stomach = stomach;
        stomach.pods().forEach((name, box) -> labels.put(name, Displays.text(instance,
                box.centre().withY(box.maxY() + 1.6), label(100))));
    }

    private static String label(double health) {
        String colour = health > 60 ? "<a>" : health > 25 ? "<e>" : "<c>";
        return "<d><l>Kuudra's Pod\n" + colour + (int) Math.ceil(health) + "%";
    }

    public void show(String pod, double health) {
        Entity label = labels.get(pod);
        if (label != null) Displays.setText(label, health <= 0 ? "<c><l>DESTROYED" : label(health));
    }

    public void breakPod(String pod) {
        KuudraArena.Box box = stomach.pods().get(pod);
        if (box == null || broken.containsKey(pod)) return;
        Map<Spot, Block> originals = new HashMap<>();
        for (int x = box.minX(); x <= box.maxX(); x++) {
            for (int y = box.minY(); y <= box.maxY(); y++) {
                for (int z = box.minZ(); z <= box.maxZ(); z++) {
                    Block block = instance.getBlock(x, y, z);
                    if (POD_BLOCKS.stream().noneMatch(block::compare)) continue;
                    originals.put(new Spot(x, y, z), block);
                    instance.setBlock(x, y, z, Block.AIR);
                }
            }
        }
        broken.put(pod, originals);
        show(pod, 0);
    }

    public void restore() {
        broken.values().forEach(originals -> originals.forEach((spot, block) -> instance.setBlock(spot.x(), spot.y(), spot.z(), block)));
        broken.clear();
        stomach.pods().keySet().forEach(pod -> show(pod, 100));
    }

    /** The pod the player is looking at within {@code reach}, if a pod block is the first solid block on the line. */
    public @Nullable String podInSight(Pos eye, Vec direction, double reach) {
        Vec step = direction.normalize().mul(0.2);
        Pos at = eye;
        for (double travelled = 0; travelled <= reach; travelled += 0.2) {
            at = at.add(step);
            int x = at.blockX();
            int y = at.blockY();
            int z = at.blockZ();
            Block block = instance.getBlock(x, y, z);
            String pod = stomach.podAt(x, y, z);
            if (pod != null && POD_SURFACE.stream().anyMatch(block::compare)) return pod;
            if (block.isSolid()) return null;
        }
        return null;
    }

    public void remove() {
        restore();
        labels.values().forEach(Displays::remove);
        labels.clear();
    }
}
