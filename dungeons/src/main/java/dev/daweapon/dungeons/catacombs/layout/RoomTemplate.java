package dev.daweapon.dungeons.catacombs.layout;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
        List<SecretAnchor> secretAnchors,
        RoomStructure structure,
        StandPoint standPoint,
        List<CryptSpot> crypts,
        Rotation fixedRotation
) {
    public RoomTemplate {
        tags = List.copyOf(tags);
        doorSockets = List.copyOf(doorSockets);
        mobSpawns = List.copyOf(mobSpawns);
        secretAnchors = List.copyOf(secretAnchors);
        crypts = List.copyOf(crypts);
    }

    public RoomTemplate(String id, String name, RoomShape shape, RoomKind kind, int secretCount, boolean placeholder,
                        List<String> tags, List<DoorSocket> doorSockets, List<MobSpawnPoint> mobSpawns,
                        List<SecretAnchor> secretAnchors) {
        this(id, name, shape, kind, secretCount, placeholder, tags, doorSockets, mobSpawns, secretAnchors, null, null,
                List.of(), null);
    }

    public Optional<RoomStructure> blocks() {
        return Optional.ofNullable(structure);
    }

    public Optional<StandPoint> stand() {
        return Optional.ofNullable(standPoint);
    }

    public Optional<Rotation> rotationLock() {
        return Optional.ofNullable(fixedRotation);
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

    public boolean restrictsDoors() {
        return !doorSockets.isEmpty();
    }

    public boolean allowsDoor(Rotation rotation, Cell rotatedCell, Side side) {
        return !restrictsDoors() || doorSockets(rotation).contains(new DoorSocket(rotatedCell, side));
    }

    public List<Rotation> rotationsAllowing(Set<Side> sides) {
        List<Rotation> rotations = new ArrayList<>();
        for (Rotation rotation : Rotation.values()) {
            if (sides.stream().allMatch(side -> allowsDoor(rotation, new Cell(0, 0), side))) rotations.add(rotation);
        }
        return rotations;
    }
}
