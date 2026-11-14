package dev.daweapon.commons;

import lombok.Getter;

@Getter
// Enumerates every kind of backend server on the network and whether it belongs to SkyBlock.
public enum ServerType {
	SKYBLOCK_ISLAND(true),
	SKYBLOCK_HUB(true),
	SKYBLOCK_SPIDERS_DEN(true),
	SKYBLOCK_THE_END(true),
	SKYBLOCK_CRIMSON_ISLE(true),
	SKYBLOCK_DUNGEON_HUB(true),
	SKYBLOCK_THE_FARMING_ISLANDS(true),
	SKYBLOCK_GOLD_MINE(true),
	SKYBLOCK_DEEP_CAVERNS(true),
	SKYBLOCK_DWARVEN_MINES(true),
	SKYBLOCK_CRYSTAL_HOLLOWS(true),
	SKYBLOCK_THE_PARK(true),
	SKYBLOCK_GALATEA(true),
	SKYBLOCK_TORRHUS_CANYON(true),
	SKYBLOCK_BACKWATER_BAYOU(true),
	SKYBLOCK_JERRYS_WORKSHOP(true),
	SKYBLOCK_KUUDRA(true),
	SKYBLOCK_THE_GARDEN(true),
	SKYBLOCK_CATACOMBS(true),
	PROTOTYPE_LOBBY(false)
	;

	private final boolean isSkyBlock;

	ServerType(boolean isSkyBlock) {
		this.isSkyBlock = isSkyBlock;
	}

	public static boolean isServerType(String type) {
		for (ServerType a : values())
			if (type.equalsIgnoreCase(a.name())) return true;

		return false;
	}

	public static ServerType getSkyblockServer(String name) {
		if (!name.startsWith("SKYBLOCK_")) {
			return valueOf("SKYBLOCK_" + name.toUpperCase());
		} else {
			return valueOf(name);
		}
	}

	public String formatName() {
		return StringUtility.toNormalCase(name());
	}

	public String skyblockName() {
		return name().replace("SKYBLOCK_", "");
	}
}
