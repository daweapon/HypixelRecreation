package dev.daweapon.type.catacombs.engine;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.dungeons.catacombs.layout.FloorLayout;
import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import dev.daweapon.dungeons.catacombs.layout.RoomKind;
import dev.daweapon.type.catacombs.arena.RoomFeatures;
import dev.daweapon.type.catacombs.engine.look.Skin;
import dev.daweapon.type.catacombs.secret.SecretMessages;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import org.tinylog.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The Fairy Room's fairies ({@code configuration/skyblock/dungeons/fairies.json}): four of them drift around the tree for
 * the whole run, as in both captures. Killing one revives a dead party member (the killer first if they are the ghost)
 * and drops a Revive Stone, per the wiki.
 */
final class FairyRoom {
    public static final Path FILE = Path.of("./configuration/skyblock/dungeons/fairies.json");
    static final double REVIVE_STONE_PERCENT = 100;
    private static final double DRIFT_SPEED = 0.15;
    private static final double ARRIVED = 0.2;

    record Config(Skin head, int count, int health, List<String> names) {
        static Config load(Path file) throws IOException {
            JsonObject root = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
            List<String> names = new ArrayList<>();
            root.getAsJsonArray("names").forEach(name -> names.add(name.getAsString()));
            return new Config(Skin.parse(root.getAsJsonObject("head")), root.get("count").getAsInt(), root.get("health").getAsInt(),
                    List.copyOf(names));
        }
    }

    private final DungeonHost host;
    private final FloorLayout layout;
    private final Map<Fairy, Pos> targets = new ConcurrentHashMap<>();
    private RoomFeatures.Box zone;
    private Task drift;

    FairyRoom(DungeonHost host, FloorLayout layout) {
        this.host = host;
        this.layout = layout;
    }

    void start() {
        List<PlacedRoom> rooms = layout.roomsOfKind(RoomKind.FAIRY);
        if (rooms.isEmpty() || !host.arena().blocks().isReal(rooms.getFirst())) return;
        zone = RoomFeatures.shared().fairies(rooms.getFirst()).orElse(null);
        if (zone == null) return;
        Config config;
        try {
            config = Config.load(FILE);
        } catch (IOException | RuntimeException exception) {
            Logger.error(exception, "Could not read the dungeon fairies from {}", FILE);
            return;
        }
        List<String> names = new ArrayList<>(config.names());
        Collections.shuffle(names, ThreadLocalRandom.current());
        for (int index = 0; index < config.count() && index < names.size(); index++) {
            Fairy fairy = new Fairy(names.get(index), config.health(), config.head(), this::killed);
            fairy.setInstance(host.instance(), randomSpot());
            targets.put(fairy, randomSpot());
        }
        drift = MinecraftServer.getSchedulerManager().buildTask(this::drift).repeat(TaskSchedule.nextTick()).schedule();
    }

    private Pos randomSpot() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return new Pos(random.nextDouble(zone.minX(), zone.maxX()), random.nextDouble(zone.minY(), zone.maxY()),
                random.nextDouble(zone.minZ(), zone.maxZ()));
    }

    private void drift() {
        for (Map.Entry<Fairy, Pos> entry : targets.entrySet()) {
            Fairy fairy = entry.getKey();
            if (fairy.isRemoved() || fairy.getInstance() == null) continue;
            Pos at = fairy.getPosition();
            Pos target = entry.getValue();
            double distance = at.distance(target);
            if (distance < ARRIVED) {
                entry.setValue(randomSpot());
                continue;
            }
            double step = Math.min(DRIFT_SPEED, distance) / distance;
            fairy.teleport(at.add((target.x() - at.x()) * step, (target.y() - at.y()) * step, (target.z() - at.z()) * step));
        }
    }

    private void killed(Fairy fairy, SkyBlockPlayer killer) {
        targets.remove(fairy);
        host.reviveGhost(killer);
        if (LootRoll.roll(killer.getUuid(), ItemType.REVIVE_STONE, REVIVE_STONE_PERCENT)) {
            killer.addAndUpdateItem(ItemType.REVIVE_STONE);
            host.broadcast(SecretMessages.obtained(killer, ItemType.REVIVE_STONE));
        }
    }

    void dispose() {
        if (drift != null) drift.cancel();
        targets.keySet().forEach(Fairy::remove);
        targets.clear();
    }

    int alive() {
        return targets.size();
    }
}
