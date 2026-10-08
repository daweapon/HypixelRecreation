package net.swofty.type.skyblockgeneric.crimson.faction;

import lombok.Getter;
import net.minestom.server.item.Material;
import net.swofty.type.skyblockgeneric.levels.SkyBlockXPSource;
import org.jetbrains.annotations.Nullable;

@Getter
public enum CrimsonFaction {
    MAGE("Mage", "<5>", "ቾ", "Scarleton", Material.PURPLE_BANNER, SkyBlockXPSource.MAGE_REPUTATION),
    BARBARIAN("Barbarian", "<c>", "⚒", "Dragontail", Material.RED_BANNER, SkyBlockXPSource.BARBARIAN_REPUTATION),
    ;

    private final String displayName;
    private final String colour;
    private final String icon;
    private final String town;
    private final Material bannerMaterial;
    private final SkyBlockXPSource xpSource;

    CrimsonFaction(String displayName, String colour, String icon, String town, Material bannerMaterial,
                   SkyBlockXPSource xpSource) {
        this.displayName = displayName;
        this.colour = colour;
        this.icon = icon;
        this.town = town;
        this.bannerMaterial = bannerMaterial;
        this.xpSource = xpSource;
    }

    public String coloured() {
        return colour + displayName;
    }

    public CrimsonFaction opposite() {
        return this == MAGE ? BARBARIAN : MAGE;
    }

    public static @Nullable CrimsonFaction byName(String name) {
        for (CrimsonFaction faction : values()) {
            if (faction.name().equalsIgnoreCase(name)) return faction;
        }
        return null;
    }
}
