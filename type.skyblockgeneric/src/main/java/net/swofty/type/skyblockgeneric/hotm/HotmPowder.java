package net.swofty.type.skyblockgeneric.hotm;

import lombok.Getter;
import net.minestom.server.item.Material;

@Getter
public enum HotmPowder {
    MITHRIL("Mithril Powder", "<2>", Material.GREEN_DYE),
    GEMSTONE("Gemstone Powder", "<d>", Material.PINK_DYE),
    GLACITE("Glacite Powder", "<b>", Material.LIGHT_BLUE_DYE);

    private final String displayName;
    private final String colour;
    private final Material icon;

    HotmPowder(String displayName, String colour, Material icon) {
        this.displayName = displayName;
        this.colour = colour;
        this.icon = icon;
    }

    public String coloured() {
        return colour + displayName;
    }
}
