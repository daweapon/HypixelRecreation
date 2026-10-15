package dev.daweapon.dungeons.catacombs.layout;

import java.util.List;

public record RoomTemplate(
        String id,
        String name,
        RoomShape shape,
        RoomKind kind,
        int secretCount,
        boolean placeholder,
        List<String> tags,
        List<DoorSocket> doorSockets,
        List<MobSpawnPoint> mobSpawns,
        List<SecretAnchor> secretAnchors
) {
    public RoomTemplate {
        tags = List.copyOf(tags);
        doorSockets = List.copyOf(doorSockets);
        mobSpawns = List.copyOf(mobSpawns);
        secretAnchors = List.copyOf(secretAnchors);
    }

    public List<MobSpawnPoint> starredSpawns() {
        return mobSpawns.stream().filter(MobSpawnPoint::starred).toList();
    }

    public boolean hasStarredMobs() {
        return mobSpawns.stream().anyMatch(MobSpawnPoint::starred);
    }

    public List<DoorSocket> doorSockets(Rotation rotation) {
        return doorSockets.stream().map(socket -> socket.rotate(shape, rotation)).toList();
    }
}
