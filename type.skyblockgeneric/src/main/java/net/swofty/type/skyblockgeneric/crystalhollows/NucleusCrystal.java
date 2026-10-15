package net.swofty.type.skyblockgeneric.crystalhollows;

import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;

import java.util.Locale;

@Getter
public enum NucleusCrystal {
    JADE("Jade", "<a>", "§a", NamedTextColor.GREEN, ItemType.FINE_JADE_GEM),
    AMBER("Amber", "<6>", "§6", NamedTextColor.GOLD, ItemType.FINE_AMBER_GEM),
    AMETHYST("Amethyst", "<5>", "§5", NamedTextColor.DARK_PURPLE, ItemType.FINE_AMETHYST_GEM),
    SAPPHIRE("Sapphire", "<b>", "§b", NamedTextColor.AQUA, ItemType.FINE_SAPPHIRE_GEM),
    TOPAZ("Topaz", "<e>", "§e", NamedTextColor.YELLOW, ItemType.FINE_TOPAZ_GEM);

    private final String displayName;
    private final String colour;
    private final String legacyColour;
    private final NamedTextColor glowColour;
    private final ItemType fineGem;

    NucleusCrystal(String displayName, String colour, String legacyColour, NamedTextColor glowColour, ItemType fineGem) {
        this.displayName = displayName;
        this.colour = colour;
        this.legacyColour = legacyColour;
        this.glowColour = glowColour;
        this.fineGem = fineGem;
    }

    public Text coloured() {
        return Text.of(colour + displayName + " Crystal");
    }

    public String itemModel() {
        return "hypixel_skyblock:item/uncategorized/" + name().toLowerCase(Locale.ROOT) + "_crystal";
    }
}
