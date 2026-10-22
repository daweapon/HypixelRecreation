package dev.daweapon.type.skyblockgeneric.island.pad;

import org.jetbrains.annotations.Nullable;

public enum TeleportPadDirection {
    PLAYER("Player Direction", null),
    NORTH("North", 180f),
    NORTH_EAST("North East", 225f),
    EAST("East", 270f),
    SOUTH_EAST("South East", 315f),
    SOUTH("South", 0f),
    SOUTH_WEST("South West", 45f),
    WEST("West", 90f),
    NORTH_WEST("North West", 135f);

    private final String label;
    private final Float yaw;

    TeleportPadDirection(String label, Float yaw) {
        this.label = label;
        this.yaw = yaw;
    }

    public String label() {
        return label;
    }

    public @Nullable Float yaw() {
        return yaw;
    }

    public float yawOr(float current) {
        return yaw == null ? current : yaw;
    }

    public static TeleportPadDirection parse(String raw) {
        for (TeleportPadDirection direction : values()) {
            if (direction.name().equals(raw)) return direction;
        }
        return PLAYER;
    }
}
