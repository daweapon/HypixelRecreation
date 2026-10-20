package dev.daweapon.type.skyblockgeneric.romero;

import dev.daweapon.commons.skyblock.item.ItemType;
import lombok.Getter;

@Getter
public enum RomeroRing {
    SHINY_YELLOW_ROCK("Shiny Yellow Rock", ""),
    YELLOW_ROCK_OF_LOVE("Yellow Rock of Love", ""),
    MEDIOCRE("Mediocre Ring of Love", "<a>"),
    RUBBISH("Rubbish Ring of Love", "<a>"),
    MODEST("Modest Ring of Love", "<9>"),
    REFINED("Refined Ring of Love", "<9>"),
    CLASSY("Classy Ring of Love", "<9>"),
    EXQUISITE("Exquisite Ring of Love", "<5>"),
    INVALUABLE("Invaluable Ring of Love", "<5>"),
    LEGENDARY("Legendary Ring of Love", "<6>");

    private final String displayName;
    private final String colour;

    RomeroRing(String displayName, String colour) {
        this.displayName = displayName;
        this.colour = colour;
    }

    public ItemType itemType() {
        return ItemType.get("WEDDING_RING_" + ordinal());
    }

    public String coloured() {
        return colour + displayName;
    }

    public String upgradeMessage(RomeroRing next) {
        return "<e>Your " + coloured() + " <e>was upgraded to " + next.coloured() + "<e>!";
    }
}
