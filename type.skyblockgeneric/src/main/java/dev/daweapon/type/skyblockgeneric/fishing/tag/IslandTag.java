package dev.daweapon.type.skyblockgeneric.fishing.tag;

import dev.daweapon.commons.ServerType;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.fishing.FishingContext;

/**
 * Gates a catch to one whole island (server), so every sub-area counts without listing each region.
 */
public record IslandTag(String id, ServerType server) implements FishingTag {

    public static final IslandTag CRIMSON_ISLE = new IslandTag("CRIMSON", ServerType.SKYBLOCK_CRIMSON_ISLE);

    @Override
    public boolean isAvailable(FishingContext context) {
        return HypixelConst.getTypeLoader() != null && HypixelConst.getTypeLoader().getType() == server;
    }
}
