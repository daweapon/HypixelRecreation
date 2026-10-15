package dev.daweapon.type.skyblockgeneric.garden.greenhouse;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public enum Surface {
    FARMLAND("Farmland", null),
    SOUL_SAND("Soul Sand", "SOUL_SAND"),
    SAND("Sand", "SAND"),
    MYCELIUM("Mycelium", "MYCELIUM"),
    DIRT("Dirt", "DIRT"),
    END_STONE("End Stone", "END_STONE");

    private final String displayName;
    private final String itemId;

    Surface(String displayName, @Nullable String itemId) {
        this.displayName = displayName;
        this.itemId = itemId;
    }

    public String displayName() {
        return displayName;
    }

    public @Nullable String itemId() {
        return itemId;
    }

    public static @Nullable Surface ofItem(@Nullable String itemId) {
        if (itemId == null) return null;
        for (Surface surface : values()) {
            if (itemId.equals(surface.itemId)) return surface;
        }
        return null;
    }

    public static @Nullable Surface parse(@Nullable String name) {
        if (name == null) return null;
        for (Surface surface : values()) {
            if (surface.displayName.equalsIgnoreCase(name.trim()) || surface.name().equalsIgnoreCase(name.trim())) return surface;
        }
        return null;
    }

    public static List<Surface> allowed(@Nullable String requirement) {
        List<Surface> result = new ArrayList<>();
        if (requirement == null) return result;
        for (String part : requirement.split("/")) {
            Surface surface = parse(part);
            if (surface != null) result.add(surface);
        }
        return result;
    }

    public static boolean accepts(@Nullable String requirement, Surface surface) {
        List<Surface> allowed = allowed(requirement);
        return allowed.isEmpty() || allowed.contains(surface);
    }
}
