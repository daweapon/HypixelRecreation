package dev.daweapon.commons;

import java.nio.file.Path;

// Enumerates the pre-built Polar world files shipped with the server and their on-disk paths.
public enum CustomWorlds {
	SKYBLOCK_ISLAND_TEMPLATE("hypixel_skyblock_island_template"),
	SKYBLOCK_GARDEN_TEMPLATE("hypixel_skyblock_garden_template"),
	SKYBLOCK_HUB("hypixel_skyblock_hub"),
	SKYBLOCK_SPIDERS_DEN("hypixel_skyblock_spiders_den"),
	SKYBLOCK_THE_END("hypixel_skyblock_the_end"),
	SKYBLOCK_CRIMSON_ISLE("hypixel_skyblock_crimson_isle"),
	SKYBLOCK_GOLD_MINE("hypixel_skyblock_gold_mine"),
	SKYBLOCK_DEEP_CAVERNS("hypixel_skyblock_deep_caverns"),
	SKYBLOCK_DWARVEN_MINES("hypixel_skyblock_dwarven_mines"),
	SKYBLOCK_CRYSTAL_HOLLOWS("hypixel_skyblock_crystal_hollows"),
	SKYBLOCK_THE_PARK("hypixel_skyblock_the_park"),
	SKYBLOCK_GALATEA("hypixel_skyblock_galatea"),
	SKYBLOCK_TORRHUS_CANYON("hypixel_skyblock_torrhus_canyon"),
	SKYBLOCK_BACKWATER_BAYOU("hypixel_skyblock_backwater_bayou"),
	SKYBLOCK_JERRYS_WORKSHOP("hypixel_skyblock_jerrys_workshop"),
	SKYBLOCK_DUNGEON_HUB("hypixel_skyblock_dungeon_hub"),
	PROTOTYPE_LOBBY("hypixel_prototype_lobby"),
	BEDWARS_LOBBY("hypixel_bedwars_lobby"),
	MURDER_MYSTERY_LOBBY("hypixel_murder_mystery_lobby"),
	SKYWARS_LOBBY("hypixel_skywars_lobby"),
    RAVENGARD_LOBBY("hypixel_ravengard_lobby"),
    RAVENGARD_TUTORIAL("hypixel_ravengard_tutorial"),
	MAIN_LOBBY("hypixel_main_lobby"),
	;

    private final String fileName;

    CustomWorlds(String fileName) {
        this.fileName = fileName;
	}

	public Path getPath() {
        return Path.of("./configuration/world/" + fileName + ".polar");
	}
}
