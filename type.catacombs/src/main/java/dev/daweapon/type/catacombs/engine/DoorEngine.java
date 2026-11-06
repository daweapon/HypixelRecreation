package dev.daweapon.type.catacombs.engine;

import dev.daweapon.commons.text.Text;
import dev.daweapon.dungeons.catacombs.layout.DoorType;
import dev.daweapon.dungeons.catacombs.layout.LayoutDoor;
import dev.daweapon.type.catacombs.arena.DoorFrame;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Wither and Blood doors: a player next to one while the party holds its key opens it and spends the key. The door
 * sinks into the floor with the {@link GateLift} effect, the opener hears the captured pling and the Blood door adds a
 * ghast cry.
 */
final class DoorEngine {
    private static final double REACH = 3.0;
    private static final long HINT_COOLDOWN_MILLIS = 4_000;
    private static final Sound OPENED = Sound.sound(Key.key("block.note_block.pling"), Sound.Source.BLOCK, 8f, 4.047619f);
    private static final Sound BLOOD_OPENED = Sound.sound(Key.key("entity.ghast.hurt"), Sound.Source.HOSTILE, 2f, 0.4920635f);

    private final DungeonHost host;
    private final List<LayoutDoor> doors;
    private final Set<LayoutDoor> opened = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Long> lastHint = new HashMap<>();

    DoorEngine(DungeonHost host, List<LayoutDoor> doors) {
        this.host = host;
        this.doors = doors;
    }

    void poll() {
        for (LayoutDoor door : doors) {
            if (door.plain() || opened.contains(door)) continue;
            DoorFrame frame = DoorFrame.of(door);
            for (SkyBlockPlayer player : host.alivePlayers()) {
                if (!near(player, frame)) continue;
                if (DoorRules.canOpen(door.type(), host.keys())) {
                    open(door, player, true);
                    break;
                }
                hint(player, door.type());
            }
        }
    }

    private static boolean near(SkyBlockPlayer player, DoorFrame frame) {
        double dx = player.getPosition().x() - frame.centreX();
        double dz = player.getPosition().z() - frame.centreZ();
        return Math.sqrt(dx * dx + dz * dz) <= REACH && Math.abs(player.getPosition().y() - frame.centreY()) <= REACH + 1;
    }

    private void hint(SkyBlockPlayer player, DoorType type) {
        long now = System.currentTimeMillis();
        if (now - lastHint.getOrDefault(player.getUuid(), 0L) < HINT_COOLDOWN_MILLIS) return;
        lastHint.put(player.getUuid(), now);
        DoorRules.requiredKey(type).ifPresent(key -> player.sendMessage("<c>You need the " + key.coloured() + " <c>to open this door!"));
    }

    boolean open(LayoutDoor door, SkyBlockPlayer opener, boolean spendKey) {
        if (opened.contains(door)) return false;
        if (spendKey && !DoorRules.tryOpen(door.type(), host.keys())) return false;
        opened.add(door);
        DoorFrame frame = DoorFrame.of(door);
        Block appearance = host.arena().blocks().gates().appearanceOf(door).orElse(Block.COAL_BLOCK);
        GateLift.run(host.instance(), DoorFrame.passage(door).blocks(), appearance);
        Pos at = opener != null ? opener.getPosition() : new Pos(frame.centreX(), frame.centreY(), frame.centreZ());
        host.instance().playSound(OPENED, at.x(), at.y(), at.z());
        if (door.bloodDoor()) host.instance().playSound(BLOOD_OPENED, at.x(), at.y(), at.z());
        announce(door.type(), opener);
        return true;
    }

    private void announce(DoorType type, SkyBlockPlayer opener) {
        if (type == DoorType.BLOOD) {
            host.broadcast("<c>The <c><l>BLOOD DOOR</l><c> has been opened!");
            host.broadcast("<5>A shiver runs down your spine...");
            return;
        }
        host.broadcast(opener == null
                ? Text.of("<8>A <l>WITHER</l><8> door was opened!")
                : Text.of("{}<a> opened a <8><l>WITHER</l> <a>door!", opener.getColouredName()));
    }

    boolean bloodDoorOpened() {
        return doors.stream().anyMatch(door -> door.bloodDoor() && opened.contains(door));
    }

    boolean isOpen(LayoutDoor door) {
        return opened.contains(door);
    }

    /** The next closed key door along the critical path, in the order the party meets them. */
    Optional<LayoutDoor> nextClosed(List<Integer> criticalPath) {
        for (int step = 0; step < criticalPath.size() - 1; step++) {
            int from = criticalPath.get(step);
            int to = criticalPath.get(step + 1);
            for (LayoutDoor door : doors) {
                if (door.connects(from) && door.connects(to) && !door.plain() && !opened.contains(door)) return Optional.of(door);
            }
        }
        return Optional.empty();
    }

    long openedCount() {
        return opened.size();
    }
}
