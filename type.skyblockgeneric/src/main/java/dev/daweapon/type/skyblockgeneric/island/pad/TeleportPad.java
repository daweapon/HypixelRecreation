package dev.daweapon.type.skyblockgeneric.island.pad;

import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class TeleportPad {
    public static final int MAX_NAME_LENGTH = 24;

    private final int x;
    private final int y;
    private final int z;
    private int icon;
    private String name = "";
    private TeleportPadDirection direction = TeleportPadDirection.PLAYER;
    private @Nullable String destination;

    public TeleportPad(int x, int y, int z, int icon) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.icon = icon;
    }

    public static String key(int x, int y, int z) {
        return x + ":" + y + ":" + z;
    }

    public static String sanitizeName(String raw) {
        String cleaned = raw == null ? "" : raw.replace("<", "").replace(">", "").replace("\\", "").trim();
        return cleaned.length() > MAX_NAME_LENGTH ? cleaned.substring(0, MAX_NAME_LENGTH) : cleaned;
    }

    public String key() {
        return key(x, y, z);
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int z() {
        return z;
    }

    public int icon() {
        return icon;
    }

    public void icon(int icon) {
        this.icon = icon;
    }

    public String name() {
        return name;
    }

    public void name(String name) {
        this.name = sanitizeName(name);
    }

    public TeleportPadDirection direction() {
        return direction;
    }

    public void direction(TeleportPadDirection direction) {
        this.direction = direction;
    }

    public @Nullable String destination() {
        return destination;
    }

    public void destination(@Nullable String destination) {
        this.destination = destination;
    }

    public TeleportPadIcon iconType() {
        return TeleportPadIcon.get(icon);
    }

    public String displayName() {
        TeleportPadIcon type = iconType();
        return name.isEmpty() ? type.label() + " Teleport Pad" : type.colour() + name;
    }

    public String serialize() {
        String encodedName = Base64.getUrlEncoder().withoutPadding().encodeToString(name.getBytes(StandardCharsets.UTF_8));
        return String.join(",", String.valueOf(x), String.valueOf(y), String.valueOf(z), String.valueOf(icon),
                direction.name(), destination == null ? "" : destination, encodedName);
    }

    public static @Nullable TeleportPad deserialize(String raw) {
        String[] parts = raw.split(",", -1);
        if (parts.length != 7) return null;
        try {
            TeleportPad pad = new TeleportPad(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
            pad.direction = TeleportPadDirection.parse(parts[4]);
            pad.destination = parts[5].isEmpty() ? null : parts[5];
            pad.name = sanitizeName(new String(Base64.getUrlDecoder().decode(parts[6]), StandardCharsets.UTF_8));
            return pad;
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
