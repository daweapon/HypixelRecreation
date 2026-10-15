package dev.daweapon.type.skyblockgeneric.garden.composter;

import lombok.Getter;

@Getter
public enum ComposterUpgrade {
    SPEED("Composter Speed"),
    MULTI_DROP("Multi Drop"),
    FUEL_CAP("Fuel Cap"),
    ORGANIC_MATTER_CAP("Organic Matter Cap"),
    COST_REDUCTION("Cost Reduction");

    public static final int MAX_TIER = 25;

    private final String displayName;

    ComposterUpgrade(String displayName) {
        this.displayName = displayName;
    }
}
