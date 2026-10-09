package net.swofty.type.skyblockgeneric.crimson.kuudra.reward;

import lombok.Getter;

@Getter
public enum KuudraChest {
    FREE("Free Chest", "<a>", "FREE CHEST REWARDS"),
    PAID("Paid Chest", "<6>", "PAID CHEST REWARDS"),
    ;

    private final String displayName;
    private final String colour;
    private final String rewardsHeader;

    KuudraChest(String displayName, String colour, String rewardsHeader) {
        this.displayName = displayName;
        this.colour = colour;
        this.rewardsHeader = rewardsHeader;
    }
}
