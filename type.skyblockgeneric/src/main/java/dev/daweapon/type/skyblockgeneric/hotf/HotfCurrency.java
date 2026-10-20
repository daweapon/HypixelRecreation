package dev.daweapon.type.skyblockgeneric.hotf;

import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import lombok.Getter;

@Getter
public enum HotfCurrency {
    FOREST("Forest Whispers", "<3>", SkyBlockDataHandler.Data.FOREST_WHISPERS),
    DESERT("Desert Whispers", "<6>", SkyBlockDataHandler.Data.DESERT_WHISPERS);

    private final String displayName;
    private final String colour;
    private final SkyBlockDataHandler.Data datapoint;

    HotfCurrency(String displayName, String colour, SkyBlockDataHandler.Data datapoint) {
        this.displayName = displayName;
        this.colour = colour;
        this.datapoint = datapoint;
    }

    public String coloured() {
        return colour + displayName;
    }
}
