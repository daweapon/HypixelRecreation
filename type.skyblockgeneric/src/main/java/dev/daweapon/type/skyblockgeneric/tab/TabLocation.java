package dev.daweapon.type.skyblockgeneric.tab;

import dev.daweapon.type.generic.HypixelConst;
import lombok.Getter;
import net.minestom.server.item.Material;

import java.util.Optional;

@Getter
public enum TabLocation {
    PRIVATE_ISLAND("private_island", "Private Island", Material.GRASS_BLOCK),
    HUB("hub", "Hub", Material.OAK_DOOR),
    GARDEN("garden", "Garden", Material.WHEAT),
    THE_FARMING_ISLANDS("the_farming_islands", "The Farming Islands", Material.HAY_BLOCK),
    THE_PARK("the_park", "The Park", Material.JUNGLE_SAPLING),
    SPIDERS_DEN("spiders_den", "Spider's Den", Material.COBWEB),
    THE_END("the_end", "The End", Material.END_STONE),
    GOLD_MINE("gold_mine", "Gold Mine", Material.GOLD_INGOT),
    DEEP_CAVERNS("deep_caverns", "Deep Caverns", Material.DIAMOND),
    DWARVEN_MINES("dwarven_mines", "Dwarven Mines", Material.PRISMARINE_SHARD),
    CRYSTAL_HOLLOWS("crystal_hollows", "Crystal Hollows", Material.AMETHYST_SHARD),
    CRIMSON_ISLE("crimson_isle", "Crimson Isle", Material.NETHERRACK),
    GALATEA("galatea", "Galatea", Material.AZALEA_LEAVES),
    BACKWATER_BAYOU("backwater_bayou", "Backwater Bayou", Material.LILY_PAD),
    JERRYS_WORKSHOP("jerrys_workshop", "Jerry's Workshop", Material.SNOW_BLOCK),
    DUNGEON_HUB("dungeon_hub", "Dungeon Hub", Material.SKELETON_SKULL),
    KUUDRA("kuudra", "Kuudra", Material.MAGMA_CREAM);

    private final String id;
    private final String displayName;
    private final Material icon;

    TabLocation(String id, String displayName, Material icon) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
    }

    public String areaKey() {
        return "tablist.server_info.area." + id;
    }

    public static Optional<TabLocation> byId(String id) {
        for (TabLocation location : values()) {
            if (location.id.equals(id)) return Optional.of(location);
        }
        return Optional.empty();
    }

    public static Optional<TabLocation> current() {
        if (HypixelConst.getTypeLoader() == null) return Optional.empty();
        return switch (HypixelConst.getTypeLoader().getType()) {
            case SKYBLOCK_ISLAND -> Optional.of(PRIVATE_ISLAND);
            case SKYBLOCK_THE_GARDEN -> Optional.of(GARDEN);
            default -> byId(HypixelConst.getTypeLoader().getType().name().toLowerCase().replace("skyblock_", ""));
        };
    }
}
