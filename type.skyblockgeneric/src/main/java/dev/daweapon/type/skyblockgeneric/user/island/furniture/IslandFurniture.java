package dev.daweapon.type.skyblockgeneric.user.island.furniture;

import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class IslandFurniture {
    public static final String DATABASE_KEY = "furniture";
    public static final String SPAWN_KEY = "furniture_spawn";
    public static final int STANDARD_LIMIT = 15;
    public static final int CENTURY_CAKE_LIMIT = 20;

    private final Map<PlacedFurniture, SpawnedFurniture> placed = new LinkedHashMap<>();
    private @Nullable Pos spawnPoint;

    public static IslandFurniture deserialize(@Nullable String raw, @Nullable String rawSpawn) {
        IslandFurniture loaded = new IslandFurniture();
        if (raw != null && !raw.isBlank()) {
            for (String entry : raw.split(";")) {
                PlacedFurniture furniture = PlacedFurniture.deserialize(entry);
                if (FurnitureCatalog.get(furniture.itemId()) != null) loaded.placed.put(furniture, null);
            }
        }
        if (rawSpawn != null && !rawSpawn.isBlank()) {
            String[] parts = rawSpawn.split(",");
            loaded.spawnPoint = new Pos(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]),
                    Double.parseDouble(parts[2]), Float.parseFloat(parts[3]), 0);
        }
        return loaded;
    }

    public String serialize() {
        return placed.keySet().stream().map(PlacedFurniture::serialize).collect(Collectors.joining(";"));
    }

    public @Nullable String serializeSpawn() {
        if (spawnPoint == null) return null;
        return spawnPoint.x() + "," + spawnPoint.y() + "," + spawnPoint.z() + "," + spawnPoint.yaw();
    }

    public List<PlacedFurniture> all() {
        return List.copyOf(placed.keySet());
    }

    public @Nullable Pos spawnPoint() {
        return spawnPoint;
    }

    public void setSpawnPoint(@Nullable Pos spawnPoint) {
        this.spawnPoint = spawnPoint;
    }

    public int count(boolean centuryCakes) {
        return (int) placed.keySet().stream()
                .filter(furniture -> FurnitureCatalog.get(furniture.itemId()).isCenturyCake() == centuryCakes)
                .count();
    }

    public boolean isFull(FurnitureDefinition definition) {
        return definition.isCenturyCake()
                ? count(true) >= CENTURY_CAKE_LIMIT
                : count(false) >= STANDARD_LIMIT;
    }

    public int limitFor(FurnitureDefinition definition) {
        return definition.isCenturyCake() ? CENTURY_CAKE_LIMIT : STANDARD_LIMIT;
    }

    public @Nullable PlacedFurniture at(int x, int y, int z) {
        return placed.keySet().stream()
                .filter(furniture -> furniture.x() == x && furniture.y() == y && furniture.z() == z)
                .findFirst().orElse(null);
    }

    public void spawnAll(Instance instance) {
        new ArrayList<>(placed.keySet()).forEach(furniture -> placed.put(furniture, spawn(instance, furniture)));
    }

    public void despawnAll() {
        placed.replaceAll((furniture, spawned) -> {
            if (spawned != null) spawned.remove();
            return null;
        });
    }

    public void add(Instance instance, PlacedFurniture furniture) {
        placed.put(furniture, spawn(instance, furniture));
    }

    public void remove(PlacedFurniture furniture) {
        SpawnedFurniture spawned = placed.remove(furniture);
        if (spawned != null) spawned.remove();
    }

    public PlacedFurniture replace(Instance instance, PlacedFurniture old, PlacedFurniture updated) {
        remove(old);
        add(instance, updated);
        return updated;
    }

    private SpawnedFurniture spawn(Instance instance, PlacedFurniture furniture) {
        return FurnitureEntities.spawn(instance, this, furniture, FurnitureCatalog.get(furniture.itemId()));
    }

    SpawnedFurniture spawnedOf(PlacedFurniture furniture) {
        return placed.get(furniture);
    }

    void interact(SkyBlockPlayer player, PlacedFurniture furniture, boolean sneaking) {
        FurnitureActions.interact(this, player, furniture, sneaking);
    }
}
