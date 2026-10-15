package dev.daweapon.commons.skyblock;

import dev.daweapon.LinkedField;
import dev.daweapon.PlayerField;
import dev.daweapon.codec.Codecs;

import java.util.UUID;

// Defines the PlayerField/LinkedField keys used to store a SkyBlock profile's document and island UUID.
public final class SkyBlockProfileFields {
    public static final String NAMESPACE = "skyblock";
    public static final String ISLAND_UUID_KEY = "island_uuid";

    public static final PlayerField<String> DOCUMENT =
            PlayerField.create(NAMESPACE, "_doc", Codecs.STRING, null);

    public static final LinkedField<UUID, String> ISLAND_UUID =
            LinkedField.create("coop", ISLAND_UUID_KEY, Codecs.STRING, null, CoopLinks.COOP);

    private SkyBlockProfileFields() {}
}
