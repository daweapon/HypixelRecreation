package net.swofty.type.skyblockgeneric.hotm;

import lombok.Getter;

@Getter
public enum HotmSkyMallBuff {
    MINING_SPEED("<6>+100 Mining Speed", 22.73),
    MINING_FORTUNE("<6>+50 Mining Fortune", 22.73),
    POWDER("<a>+15% more Powder from any source", 22.73),
    COOLDOWN("<a>-20% Pickaxe Ability cooldowns", 22.73),
    GOBLINS("<6>10x chance to find Golden and Diamond Goblins", 4.55),
    TITANIUM("<f>5x Titanium drops", 4.55);

    private final String description;
    private final double weight;

    HotmSkyMallBuff(String description, double weight) {
        this.description = description;
        this.weight = weight;
    }

    public static HotmSkyMallBuff byId(String id) {
        if (id == null) return null;
        try {
            return valueOf(id);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
