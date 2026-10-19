package dev.daweapon.type.skyblockgeneric.hoppity;

import dev.daweapon.commons.ServerType;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.entity.InteractionEntity;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointChocolateFactory.ChocolateFactoryData;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateFactoryHelper;
import dev.daweapon.type.skyblockgeneric.rabbits.HoppityEggType;
import dev.daweapon.type.skyblockgeneric.region.IslandType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.player.ResolvableProfile;
import net.minestom.server.timer.TaskSchedule;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class HuntEggManager {
    private static final int TICK_INTERVAL = 20;
    private static final int REFRESH_EVERY_TICKS = 5;
    private static final double HEAD_OFFSET = -0.6;
    private static final int MAX_SNAP = 4;
    private static final Map<String, SpawnedEgg> SPAWNED = new LinkedHashMap<>();
    private static IslandType island;
    private static Instance instance;
    private static int lastAnnounced = -1;
    private static int lastAnnouncedYear = -1;
    private static int tickCount;

    private record SpawnedEgg(ActiveEgg egg, Pos position, EntityCreature head, InteractionEntity hitbox) {
    }

    private HuntEggManager() {
    }

    public static IslandType islandOf(ServerType type) {
        String name = type.name().replace("SKYBLOCK_", "");
        for (IslandType candidate : IslandType.values()) {
            if (candidate.name().equals(name)) return candidate;
        }
        return null;
    }

    public static IslandType currentIsland() {
        return island;
    }

    public static void start(Instance targetInstance, ServerType type) {
        IslandType resolved = islandOf(type);
        if (resolved == null || !EggLocationCatalogue.get().hasEggs(resolved)) return;
        island = resolved;
        instance = targetInstance;
        long elapsed = SkyBlockCalendar.getElapsed();
        lastAnnounced = HuntEggSchedule.isHuntActive(elapsed) ? HuntEggSchedule.latestSpawnNumber(elapsed) : -1;
        lastAnnouncedYear = HuntEggSchedule.yearOf(elapsed);
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            try {
                tick();
            } catch (RuntimeException exception) {
                org.tinylog.Logger.error(exception, "Hunt egg tick failed");
            }
            return TaskSchedule.tick(TICK_INTERVAL);
        });
    }

    public static boolean isRunning() {
        return island != null;
    }

    public static List<ActiveEgg> activeEggs() {
        if (island == null) return List.of();
        return HuntEggSchedule.activeEggs(SkyBlockCalendar.getElapsed(), island, EggLocationCatalogue.get());
    }

    public static ActiveEgg find(String spawnId) {
        SpawnedEgg spawned = SPAWNED.get(spawnId);
        return spawned == null ? null : spawned.egg();
    }

    public static ActiveEgg nearestUnclaimed(SkyBlockPlayer player) {
        ChocolateFactoryData data = ChocolateFactoryHelper.getData(player);
        ActiveEgg nearest = null;
        double best = Double.MAX_VALUE;
        for (ActiveEgg egg : activeEggs()) {
            if (data.hasClaimedSpawn(egg.spawnId())) continue;
            double distance = player.getPosition().distanceSquared(position(egg));
            if (distance < best) {
                best = distance;
                nearest = egg;
            }
        }
        return nearest;
    }

    public static Pos position(ActiveEgg egg) {
        SpawnedEgg spawned = SPAWNED.get(egg.spawnId());
        return spawned != null ? spawned.position() : rawPosition(egg);
    }

    private static Pos rawPosition(ActiveEgg egg) {
        EggLocation location = egg.location();
        return new Pos(location.x() + 0.5, location.y(), location.z() + 0.5);
    }

    private static Pos snapAboveSolid(Pos position) {
        instance.loadChunk(position.chunkX(), position.chunkZ()).join();
        Pos candidate = position;
        for (int step = 0; step <= MAX_SNAP; step++) {
            net.minestom.server.instance.block.Block block = instance.getBlock(candidate);
            if (block.isAir() || block.isLiquid()) return candidate;
            candidate = candidate.add(0, 1, 0);
        }
        return position;
    }

    private static void tick() {
        long elapsed = SkyBlockCalendar.getElapsed();
        Map<String, ActiveEgg> desired = new HashMap<>();
        for (ActiveEgg egg : activeEggs()) desired.put(egg.spawnId(), egg);

        for (String spawnId : new ArrayList<>(SPAWNED.keySet())) {
            if (!desired.containsKey(spawnId)) remove(SPAWNED.remove(spawnId));
        }
        for (ActiveEgg egg : desired.values()) {
            if (!SPAWNED.containsKey(egg.spawnId())) SPAWNED.put(egg.spawnId(), spawn(egg));
        }
        announceNewSpawn(elapsed);
        if (++tickCount % REFRESH_EVERY_TICKS == 0) refreshVisibility();
    }

    public static void refreshVisibility() {
        for (SpawnedEgg spawned : SPAWNED.values()) {
            spawned.head().updateViewableRule();
            spawned.hitbox().updateViewableRule();
        }
    }

    private static void announceNewSpawn(long elapsed) {
        int year = HuntEggSchedule.yearOf(elapsed);
        if (!HuntEggSchedule.isHuntActive(elapsed)) {
            lastAnnounced = -1;
            lastAnnouncedYear = year;
            return;
        }
        int latest = HuntEggSchedule.latestSpawnNumber(elapsed);
        if (latest < 0 || (year == lastAnnouncedYear && latest <= lastAnnounced)) return;
        lastAnnounced = latest;
        lastAnnouncedYear = year;
        HoppityEggType type = HuntEggSchedule.eggType(latest);
        for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
            player.sendMessage("<d><l>HOPPITY'S HUNT</l> <d>A " + HoppityEggService.typeColor(type) + "{} <d>has appeared!", type.getDisplayName());
        }
    }

    private static boolean visibleTo(Player viewer, String spawnId) {
        if (!(viewer instanceof SkyBlockPlayer player) || player.getSkyblockDataHandler() == null) return true;
        return !ChocolateFactoryHelper.getData(player).hasClaimedSpawn(spawnId);
    }

    private static SpawnedEgg spawn(ActiveEgg egg) {
        Pos position = snapAboveSolid(rawPosition(egg));
        EggHead head = new EggHead(egg.type());
        head.updateViewableRule(viewer -> visibleTo(viewer, egg.spawnId()));
        head.setInstance(instance, position.add(0, HEAD_OFFSET, 0));

        InteractionEntity hitbox = new InteractionEntity(1f, 1f, (clicker, event) -> {
            if (clicker instanceof SkyBlockPlayer player) onClick(player, egg);
        });
        hitbox.updateViewableRule(viewer -> visibleTo(viewer, egg.spawnId()));
        hitbox.setInstance(instance, position);
        return new SpawnedEgg(egg, position, head, hitbox);
    }

    private static void onClick(SkyBlockPlayer player, ActiveEgg egg) {
        if (HoppityEggService.claim(player, egg)) refreshVisibility();
    }

    private static void remove(SpawnedEgg spawned) {
        spawned.head().remove();
        spawned.hitbox().remove();
    }

    private static final class EggHead extends EntityCreature {
        EggHead(HoppityEggType type) {
            super(EntityType.ARMOR_STAND);
            setInvisible(true);
            ArmorStandMeta meta = (ArmorStandMeta) getEntityMeta();
            meta.setCustomNameVisible(false);
            meta.setHasNoGravity(true);
            meta.setNotifyAboutChanges(false);
            meta.setMarker(true);
            setHelmet(ItemStack.builder(Material.PLAYER_HEAD)
                    .set(DataComponents.PROFILE, new ResolvableProfile(new PlayerSkin(encode(type.getTextureHash()), null)))
                    .build());
        }

        private static String encode(String hash) {
            String json = "{\"textures\":{\"SKIN\":{\"url\":\"http://textures.minecraft.net/texture/" + hash + "\"}}}";
            return Base64.getEncoder().encodeToString(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
    }
}
