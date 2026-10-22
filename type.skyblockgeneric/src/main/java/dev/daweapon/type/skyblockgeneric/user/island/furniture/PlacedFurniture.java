package dev.daweapon.type.skyblockgeneric.user.island.furniture;

import org.jetbrains.annotations.Nullable;

public record PlacedFurniture(String itemId, int x, int y, int z, int yaw, @Nullable String data) {
    public static final int YAW_STEP = 45;

    public PlacedFurniture withYaw(int newYaw) {
        return new PlacedFurniture(itemId, x, y, z, Math.floorMod(newYaw, 360), data);
    }

    public PlacedFurniture withData(@Nullable String newData) {
        return new PlacedFurniture(itemId, x, y, z, yaw, newData);
    }

    public String serialize() {
        return itemId + "," + x + "," + y + "," + z + "," + yaw + "," + (data == null ? "" : data);
    }

    public static PlacedFurniture deserialize(String raw) {
        String[] parts = raw.split(",", 6);
        String data = parts.length > 5 && !parts[5].isEmpty() ? parts[5] : null;
        return new PlacedFurniture(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]),
                Integer.parseInt(parts[3]), Integer.parseInt(parts[4]), data);
    }

    public static int snapYaw(float yaw) {
        return Math.floorMod(Math.round(yaw / YAW_STEP) * YAW_STEP, 360);
    }
}
