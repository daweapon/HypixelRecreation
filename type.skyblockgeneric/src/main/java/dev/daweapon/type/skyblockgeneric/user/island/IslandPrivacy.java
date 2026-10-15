package dev.daweapon.type.skyblockgeneric.user.island;

// Island visitation privacy tiers, persisted per-profile via SkyBlockDataHandler.Data.ISLAND_PRIVACY.
public enum IslandPrivacy {
    PUBLIC,
    FRIENDS_ONLY,
    PRIVATE;

    // Datapoints round-trip through Jackson as a quoted JSON string, and a raw Mongo read (no live
    // handler loaded) never unwraps that - strip the quotes the same way ProfileSwitcher.islandOf does.
    public static IslandPrivacy fromStored(String raw) {
        if (raw == null) return PUBLIC;
        String trimmed = raw.replace("\"", "").trim();
        if (trimmed.isEmpty()) return PUBLIC;
        try {
            return valueOf(trimmed);
        } catch (IllegalArgumentException exception) {
            return PUBLIC;
        }
    }
}
