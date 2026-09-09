package net.swofty.type.generic;

import net.swofty.commons.Songs;

// HypixelTypeLoader specialization for SkyBlock-type servers, adding an optional island song hook.
public interface SkyBlockTypeLoader extends HypixelTypeLoader {
    default Songs getIslandSong() {
        return null;
    }
}
