package net.swofty.type.skyblockgeneric.garden.visitor;

import lombok.Getter;

@Getter
public enum VisitorRarity {
    UNCOMMON("<a>", "UNCOMMON"),
    RARE("<9>", "RARE"),
    LEGENDARY("<6>", "LEGENDARY"),
    MYTHIC("<d>", "MYTHIC"),
    SPECIAL("<c>", "SPECIAL");

    private final String colour;
    private final String label;

    VisitorRarity(String colour, String label) {
        this.colour = colour;
        this.label = label;
    }

    public boolean atLeast(VisitorRarity other) {
        return ordinal() >= other.ordinal();
    }
}
