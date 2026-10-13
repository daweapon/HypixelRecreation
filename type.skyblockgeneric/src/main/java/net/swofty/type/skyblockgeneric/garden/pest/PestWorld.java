package net.swofty.type.skyblockgeneric.garden.pest;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.swofty.type.skyblockgeneric.garden.GardenLayout;
import net.swofty.type.skyblockgeneric.garden.npc.GardenGround;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class PestWorld {
    private static final Map<Instance, Map<String, PestEntity>> LIVE = new WeakHashMap<>();

    private static final Map<Instance, Map<String, List<net.minestom.server.entity.Entity>>> TRAPS = new WeakHashMap<>();

    private record DiscoLive(double x, double y, double z, List<net.minestom.server.entity.Entity> entities) {
    }

    private static final Map<Instance, DiscoLive> DISCO = new WeakHashMap<>();

    private PestWorld() {
    }

    public static synchronized List<PestEntity> entities(Instance instance) {
        Map<String, PestEntity> live = LIVE.get(instance);
        return live == null ? List.of() : new ArrayList<>(live.values());
    }

    public static synchronized void reconcile(Instance instance, PestData data) {
        Map<String, PestEntity> live = LIVE.computeIfAbsent(instance, key -> new java.util.HashMap<>());
        live.values().removeIf(entity -> entity.isRemoved() || data.getAlive().stream().noneMatch(pest -> pest.getId().equals(entity.aliveId())));
        for (PestData.Alive alive : data.getAlive()) {
            if (live.containsKey(alive.getId())) continue;
            PestEntity entity = spawn(instance, alive);
            if (entity != null) live.put(alive.getId(), entity);
        }
    }

    public static synchronized void despawn(Instance instance, String aliveId) {
        Map<String, PestEntity> live = LIVE.get(instance);
        if (live == null) return;
        PestEntity entity = live.remove(aliveId);
        if (entity != null) entity.remove();
    }

    private static PestEntity spawn(Instance instance, PestData.Alive alive) {
        PestCatalogue.Pest pest = PestCatalogue.get().pest(alive.getType());
        int[] origin = GardenLayout.plotOrigin(alive.getPlot());
        if (pest == null || origin == null) return null;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double x = alive.hasSpawnPoint() ? alive.getSpawnX() : origin[0] + 6 + random.nextDouble() * (GardenLayout.PLOT_SIZE - 12);
        double z = alive.hasSpawnPoint() ? alive.getSpawnZ() : origin[1] + 6 + random.nextDouble() * (GardenLayout.PLOT_SIZE - 12);
        PestEntity entity = new PestEntity(pest, alive.getId(), alive.getPlot());
        instance.loadChunk(new Pos(x, 72, z)).thenRun(() ->
                entity.setInstance(instance, GardenGround.onGround(instance, new Pos(x, 72, z))));
        return entity;
    }

    public static synchronized void reconcileDisco(Instance instance, PestData data) {
        if (instance == null) return;
        PestData.Disco disco = data.getDisco();
        DiscoLive live = DISCO.get(instance);
        if (live != null && disco != null && live.x() == disco.getX() && live.y() == disco.getY() && live.z() == disco.getZ()) return;
        if (live != null) {
            live.entities().forEach(net.minestom.server.entity.Entity::remove);
            DISCO.remove(instance);
        }
        if (disco == null) return;
        Pos at = new Pos(disco.getX(), disco.getY(), disco.getZ());
        net.minestom.server.entity.Entity display = new net.minestom.server.entity.Entity(net.minestom.server.entity.EntityType.ITEM_DISPLAY);
        display.setNoGravity(true);
        display.editEntityMeta(net.minestom.server.entity.metadata.display.ItemDisplayMeta.class, meta -> {
            meta.setItemStack(net.minestom.server.item.ItemStack.of(net.minestom.server.item.Material.JUKEBOX));
            meta.setScale(new net.minestom.server.coordinate.Vec(0.9, 0.9, 0.9));
        });
        net.swofty.type.generic.entity.InteractionEntity click = new net.swofty.type.generic.entity.InteractionEntity(1f, 1f,
                (player, event) -> PestDisco.interact((net.swofty.type.skyblockgeneric.user.SkyBlockPlayer) player));
        instance.loadChunk(at).thenRun(() -> {
            display.setInstance(instance, at.add(0, 0.5, 0));
            click.setInstance(instance, at);
        });
        DISCO.put(instance, new DiscoLive(disco.getX(), disco.getY(), disco.getZ(), List.of(display, click)));
    }

    public static void indicate(Instance instance, PestData data) {
        java.util.Set<Integer> plots = new java.util.TreeSet<>();
        data.getAlive().forEach(pest -> plots.add(pest.getPlot()));
        for (int plot : plots) {
            int[] origin = GardenLayout.plotOrigin(plot);
            if (origin == null) continue;
            double x = origin[0] + GardenLayout.PLOT_SIZE / 2.0;
            double z = origin[1] + GardenLayout.PLOT_SIZE / 2.0;
            for (net.minestom.server.entity.Player player : instance.getPlayers()) {
                for (int i = 0; i < 6; i++) {
                    player.sendPacket(new net.minestom.server.network.packet.server.play.ParticlePacket(
                            net.minestom.server.particle.Particle.ANGRY_VILLAGER, true, true, (float) x, (float) (74 + i * 0.7), (float) z,
                            0.5f, 0.2f, 0.5f, 0f, 2));
                }
            }
        }
        PestDisco.emitNotes(instance, data);
    }

    public static synchronized void reconcileTraps(Instance instance, PestData data) {
        if (instance == null) return;
        Map<String, List<net.minestom.server.entity.Entity>> live = TRAPS.computeIfAbsent(instance, key -> new java.util.HashMap<>());
        live.entrySet().removeIf(entry -> {
            boolean gone = data.getTraps().stream().noneMatch(trap -> trap.getId().equals(entry.getKey()));
            if (gone) entry.getValue().forEach(net.minestom.server.entity.Entity::remove);
            return gone;
        });
        for (PestData.Trap trap : data.getTraps()) {
            if (live.containsKey(trap.getId())) continue;
            PestTraps.Kind kind = PestTraps.KINDS.get(trap.getKind());
            if (kind == null) continue;
            Pos at = new Pos(trap.getX(), trap.getY(), trap.getZ());
            net.minestom.server.entity.Entity display = new net.minestom.server.entity.Entity(net.minestom.server.entity.EntityType.ITEM_DISPLAY);
            display.setNoGravity(true);
            display.editEntityMeta(net.minestom.server.entity.metadata.display.ItemDisplayMeta.class, meta -> {
                meta.setItemStack(net.minestom.server.item.ItemStack.builder(net.minestom.server.item.Material.PAPER)
                        .set(net.minestom.server.component.DataComponents.ITEM_MODEL,
                                "hypixel_skyblock:item/island_relevant/garden/pests/" + kind.model()).build());
                meta.setScale(new net.minestom.server.coordinate.Vec(1.2, 1.2, 1.2));
            });
            String id = trap.getId();
            net.swofty.type.generic.entity.InteractionEntity click = new net.swofty.type.generic.entity.InteractionEntity(0.9f, 0.9f,
                    (player, event) -> PestTraps.interact((net.swofty.type.skyblockgeneric.user.SkyBlockPlayer) player, id));
            instance.loadChunk(at).thenRun(() -> {
                display.setInstance(instance, at.add(0, 0.5, 0));
                click.setInstance(instance, at);
            });
            live.put(trap.getId(), List.of(display, click));
        }
    }
}
