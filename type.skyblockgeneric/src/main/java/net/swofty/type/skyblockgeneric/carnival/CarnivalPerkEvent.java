package net.swofty.type.skyblockgeneric.carnival;

import lombok.Getter;
import net.minestom.server.item.Material;
import net.swofty.type.skyblockgeneric.levels.SkyBlockXPSource;

/** The six events whose perks Doug sells, in his Carnival Perks menu's order. */
@Getter
public enum CarnivalPerkEvent {
    FISHING_FESTIVAL("Fishing Festival", "<b>", Material.FISHING_ROD, SkyBlockXPSource.FISHING_FESTIVAL_PERK_SHOP),
    MINING_FIESTA("Mining Fiesta", "<6>", Material.IRON_PICKAXE, SkyBlockXPSource.MINING_FIESTA_PERK_SHOP),
    MYTHOLOGICAL_RITUAL("Mythological Ritual", "<2>", Material.FEATHER, SkyBlockXPSource.MYTHOLOGICAL_RITUAL_PERK_SHOP),
    SPOOKY_FESTIVAL("Spooky Festival", "<6>", Material.JACK_O_LANTERN, SkyBlockXPSource.SPOOKY_FESTIVAL_PERK_SHOP),
    SEASON_OF_JERRY("Season of Jerry", "<c>", Material.SNOWBALL, SkyBlockXPSource.SEASON_OF_JERRY_PERK_SHOP),
    HARVEST_FEAST("Harvest Feast", "<6>", Material.GOLDEN_HOE, SkyBlockXPSource.HARVEST_FEAST_PERK_SHOP);

    private final String displayName;
    private final String color;
    private final Material icon;
    private final SkyBlockXPSource xpSource;

    CarnivalPerkEvent(String displayName, String color, Material icon, SkyBlockXPSource xpSource) {
        this.displayName = displayName;
        this.color = color;
        this.icon = icon;
        this.xpSource = xpSource;
    }

    public String colored() {
        return color + displayName;
    }
}
