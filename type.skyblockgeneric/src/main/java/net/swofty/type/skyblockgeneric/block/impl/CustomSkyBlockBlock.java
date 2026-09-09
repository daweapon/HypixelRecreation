package net.swofty.type.skyblockgeneric.block.impl;

import lombok.NonNull;
import net.minestom.server.instance.block.Block;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

// Core contract every custom SkyBlock block implementation must fulfil: display material and placement rules.
public interface CustomSkyBlockBlock {
    @NonNull
    Block getDisplayMaterial();

    @NonNull
    Boolean shouldPlace(SkyBlockPlayer player);

    @NonNull
    Boolean shouldDestroy(SkyBlockPlayer player);
}
