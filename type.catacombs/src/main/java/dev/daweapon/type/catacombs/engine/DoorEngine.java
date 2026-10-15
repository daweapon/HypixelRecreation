package dev.daweapon.type.catacombs.engine;

import dev.daweapon.commons.text.Text;
import dev.daweapon.dungeons.catacombs.layout.DoorType;
import dev.daweapon.dungeons.catacombs.layout.LayoutDoor;
import dev.daweapon.type.catacombs.arena.DoorFrame;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Wither and Blood doors: a player next to one while the party holds its key opens it and spends the key. */
final class DoorEngine {
    private static final double REACH = 3.0;
    private static final long HINT_COOLDOWN_MILLIS = 4_000;

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
        host.arena().fill(door, Block.AIR);
        host.instance().playSound(Sound.sound(Key.key("block.iron_door.open"), Sound.Source.BLOCK, 1f, 0.8f),
                frame.centreX(), frame.centreY(), frame.centreZ());
        host.instance().sendGroupedPacket(new ParticlePacket(Particle.POOF, frame.centreX(), frame.centreY(), frame.centreZ(),
                1f, 1.5f, 1f, 0.05f, 40));
        announce(door.type(), opener);
        return true;
    }

    private void announce(DoorType type, SkyBlockPlayer opener) {
        if (type == DoorType.BLOOD) {
            host.broadcast("<c>The <4>BLOOD DOOR <c>has been opened!");
            return;
        }
        host.broadcast(opener == null
                ? Text.of("<8>A WITHER door was opened!")
                : Text.of("{}<a> opened a <8>WITHER <a>door!", opener.getColouredName()));
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
