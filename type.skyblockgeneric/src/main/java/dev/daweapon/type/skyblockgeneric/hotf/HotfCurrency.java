package dev.daweapon.type.skyblockgeneric.hotf;

import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import lombok.Getter;

@Getter
public enum HotfCurrency {
    FOREST("Forest Whispers", "<3>", "FOREST_WHISPERS"),
    DESERT("Desert Whispers", "<6>", "DESERT_WHISPERS");

    private final String displayName;
    private final String colour;
    private final String datapointName;

    HotfCurrency(String displayName, String colour, String datapointName) {
        this.displayName = displayName;
        this.colour = colour;
        this.datapointName = datapointName;
    }

    public SkyBlockDataHandler.Data getDatapoint() {
        return SkyBlockDataHandler.Data.valueOf(datapointName);
    }

    public String coloured() {
        return colour + displayName;
    }
}
