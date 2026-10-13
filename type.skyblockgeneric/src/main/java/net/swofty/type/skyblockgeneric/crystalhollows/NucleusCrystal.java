package net.swofty.type.skyblockgeneric.crystalhollows;

import lombok.Getter;
import net.swofty.commons.skyblock.item.ItemType;

@Getter
public enum NucleusCrystal {
    JADE("Jade", "<a>", ItemType.FINE_JADE_GEM),
    AMBER("Amber", "<6>", ItemType.FINE_AMBER_GEM),
    AMETHYST("Amethyst", "<5>", ItemType.FINE_AMETHYST_GEM),
    SAPPHIRE("Sapphire", "<b>", ItemType.FINE_SAPPHIRE_GEM),
    TOPAZ("Topaz", "<e>", ItemType.FINE_TOPAZ_GEM);

    private final String displayName;
    private final String colour;
    private final ItemType fineGem;

    NucleusCrystal(String displayName, String colour, ItemType fineGem) {
        this.displayName = displayName;
        this.colour = colour;
        this.fineGem = fineGem;
    }

    public String coloured() {
        return colour + displayName + " Crystal";
    }
}
