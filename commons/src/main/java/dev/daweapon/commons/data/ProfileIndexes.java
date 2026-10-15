package dev.daweapon.commons.data;

import dev.daweapon.PlayerField;
import dev.daweapon.codec.Codecs;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Defines the per-module PlayerFields storing a player's selected/available profile UUIDs, and decodes their packed string format.
public final class ProfileIndexes {
    public static final String SELECTED_SEPARATOR = ";";
    public static final String PROFILE_SEPARATOR = ",";

    public static final PlayerField<String> SKYBLOCK =
            PlayerField.create("hypixel", "_profiles_index", Codecs.STRING, null);
    public static final PlayerField<String> RAVENGARD =
            PlayerField.create("ravengard", "_profiles_index", Codecs.STRING, null);

    public static final List<PlayerField<String>> ALL = List.of(SKYBLOCK, RAVENGARD);

    private ProfileIndexes() {}

    public static List<UUID> decodeProfiles(String stored) {
        List<UUID> profiles = new ArrayList<>();
        if (stored == null || stored.isEmpty()) return profiles;

        String[] parts = stored.split(SELECTED_SEPARATOR, 2);
        if (parts.length < 2 || parts[1].isEmpty()) return profiles;

        for (String profile : parts[1].split(PROFILE_SEPARATOR)) {
            UUID parsed = parse(profile);
            if (parsed != null && !profiles.contains(parsed)) profiles.add(parsed);
        }
        return profiles;
    }

    public static UUID decodeSelected(String stored) {
        if (stored == null || stored.isEmpty()) return null;
        return parse(stored.split(SELECTED_SEPARATOR, 2)[0]);
    }

    private static UUID parse(String value) {
        if (value == null || value.isEmpty()) return null;
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
