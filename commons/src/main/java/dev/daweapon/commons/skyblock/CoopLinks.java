package dev.daweapon.commons.skyblock;

import dev.daweapon.LinkType;
import dev.daweapon.PlayerField;
import dev.daweapon.codec.Codecs;

import java.util.UUID;

// Defines the linked-field key connecting a player profile to its coop group.
public final class CoopLinks {
    public static final PlayerField<UUID> COOP_KEY = PlayerField.create("skyblock", "_coop_key", Codecs.UUID, null);
    public static final LinkType<UUID> COOP = LinkType.create("coop", Codecs.UUID, COOP_KEY);

    private CoopLinks() {}
}
