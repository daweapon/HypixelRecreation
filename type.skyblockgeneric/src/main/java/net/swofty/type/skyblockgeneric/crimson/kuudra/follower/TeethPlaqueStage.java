package net.swofty.type.skyblockgeneric.crimson.kuudra.follower;

import lombok.Getter;

/**
 * The Kuudra Teeth Plaque loses one rarity per helper, then Crag turns the Decaying plaque into its
 * final Special form.
 */
@Getter
public enum TeethPlaqueStage {
    EXQUISITE("Exquisite Kuudra Teeth Plaque", "<6>", "LEGENDARY"),
    UNPLEASANT("Unpleasant Kuudra Teeth Plaque", "<5>", "EPIC"),
    GNARLY("Gnarly Kuudra Teeth Plaque", "<9>", "RARE"),
    REPULSIVE("Repulsive Kuudra Teeth Plaque", "<a>", "UNCOMMON"),
    DECAYING("Decaying Kuudra Teeth Plaque", "<f>", "COMMON"),
    FINAL("Kuudra Teeth Plaque", "<c>", "SPECIAL"),
    ;

    private final String displayName;
    private final String colour;
    private final String rarity;

    TeethPlaqueStage(String displayName, String colour, String rarity) {
        this.displayName = displayName;
        this.colour = colour;
        this.rarity = rarity;
    }

    public String coloured() {
        return colour + displayName;
    }

    public String footer() {
        return colour + "<l>" + rarity;
    }
}
