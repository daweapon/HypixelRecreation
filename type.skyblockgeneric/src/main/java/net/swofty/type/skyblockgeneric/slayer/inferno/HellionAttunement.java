package net.swofty.type.skyblockgeneric.slayer.inferno;

public enum HellionAttunement {
    ASHEN("<8>", "Ashen"),
    SPIRIT("<f>", "Spirit"),
    AURIC("<e>", "Auric"),
    CRYSTAL("<b>", "Crystal");

    private final String colour;
    private final String displayName;

    HellionAttunement(String colour, String displayName) {
        this.colour = colour;
        this.displayName = displayName;
    }

    public String colour() {
        return colour;
    }

    public String displayName() {
        return displayName;
    }

    public String coloured() {
        return colour + displayName;
    }

    public HellionAttunement next() {
        return values()[(ordinal() + 1) % values().length];
    }
}
