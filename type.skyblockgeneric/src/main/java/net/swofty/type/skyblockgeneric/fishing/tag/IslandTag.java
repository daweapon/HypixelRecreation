package net.swofty.type.skyblockgeneric.fishing.tag;

import net.swofty.commons.ServerType;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.skyblockgeneric.fishing.FishingContext;

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
