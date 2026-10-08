package net.swofty.type.skyblockgeneric.warps;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import net.swofty.commons.ServerType;
import net.swofty.commons.StringUtility;
import net.swofty.type.generic.data.datapoints.DatapointStringList;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Getter
public enum TravelScrollIslands {
	PRIVATE_ISLAND("home", "<b>Private Island",
			"f151cffdaf303673531a7651b36637cad912ba485643158e548d59b2ead5011",
			(unused) -> "Your very own chunk of SkyBlock. Nice housing for your minions.", ServerType.SKYBLOCK_ISLAND),
	SKYBLOCK_HUB("hub", "<b>SkyBlock Hub", "9c465a5d348c53d473f8115ed8923be416f35149f73ebaf5f2b05e13401e814f", (unused) -> "Where everything happens and anything is possible.",
			ServerType.SKYBLOCK_HUB, List.of(TravelScrollType.HUB_CASTLE, TravelScrollType.HUB_MUSEUM, TravelScrollType.HUB_CRYPTS, TravelScrollType.HUB_DARK_AUCTION, TravelScrollType.HUB_WIZARD_TOWER)),
	GOLD_MINE("gold", "<a>Gold Mine <7>- <b>Spawn",
			"d8573ed917444316b0b28dd9927fd68e56f6625fcfa73ad80b8770d5139891b1",
			(unused) -> "Your first stop for extended mining related activities and home to SkyBlock's local janitor Rusty", ServerType.SKYBLOCK_GOLD_MINE, SkillCategories.MINING, 1),
	DEEP_CAVERNS("deep", "<a>Deep Caverns <7>- <b>Spawn",
			"74213dc6dc4b1641defd333f4a4732cc714dd677718fa10f140a6939c12aa32b",
			(unused) -> "An island that gets progressively deeper and contains 6 layers of dangerous mobs and new resources.", ServerType.SKYBLOCK_DEEP_CAVERNS, SkillCategories.MINING, 2),
	DWARVEN_MINES("dwarven", "<a>Dwarven Mines <7>- <b>Spawn",
			"6b20b23c1aa2be0270f016b4c90d6ee6b8330a17cfef87869d6ad60b2ffbf3b5",
			(unused) -> "An island that gets progressively deeper and contains 6 layers of dangerous mobs and new resources.", ServerType.SKYBLOCK_DWARVEN_MINES, SkillCategories.MINING, 2),
	CRIMSON_ISLE("nether", "<a>Crimson Isle <7>- <b>Spawn",
			"64457b2d7acf522d1912f0d6c6139440ba9d333a6bf61a2e9440e7fb3788939e",
			(unused) -> "Fight challenging bosses, discover new Sea Creatures, complete epic quests, and join your favorite faction!",
			ServerType.SKYBLOCK_CRIMSON_ISLE, SkillCategories.COMBAT, 4, 22, "Crimson Isle Warps",
			TravelScrollType.CRIMSON_ISLE_SPAWN,
			List.of(TravelScrollType.CRIMSON_ISLE_FORGOTTEN_SKULL, TravelScrollType.CRIMSON_ISLE_WASTELAND,
					TravelScrollType.CRIMSON_ISLE_DRAGONTAIL, TravelScrollType.CRIMSON_ISLE_SCARLETON,
					TravelScrollType.CRIMSON_ISLE_SMOLDERING_TOMB))
	;

	private static final Map<String, String> WARP_ALIASES = Map.of(
			"isle", "nether", "crimson", "nether", "ci", "nether",
			"skull", "kuudra",
			"smoldering", "smold", "smoldering_tomb", "smold");

    private final String internalName;
    @Getter(AccessLevel.NONE)
    private final String descriptiveName;
    private final String texture;
    private final Function<Boolean, String> description;
    private final @NonNull ServerType serverType;
    private final List<TravelScrollType> associatedScrolls = new ArrayList<>();

	private final SkillCategories associatedSkill;
	private final Integer islandTier;
	private final int requiredSkillLevel;
	private final @Nullable String warpMenuTitle;
	/** When set, the island's own warp is unlocked by consuming this scroll rather than by visiting. */
	private final @Nullable TravelScrollType unlockScroll;

	TravelScrollIslands(String internalName, String descriptiveName, String texture, Function<Boolean, String> description,
						@NonNull ServerType serverType, SkillCategories associatedSkill, Integer islandTier, int requiredSkillLevel,
						String warpMenuTitle, TravelScrollType unlockScroll, List<TravelScrollType> associatedScrolls) {
		this.internalName = internalName;
		this.descriptiveName = descriptiveName;
		this.texture = texture;
		this.description = description;
		this.serverType = serverType;
		this.associatedSkill = associatedSkill;
		this.islandTier = islandTier;
		this.requiredSkillLevel = requiredSkillLevel;
		this.warpMenuTitle = warpMenuTitle;
		this.unlockScroll = unlockScroll;
		this.associatedScrolls.addAll(associatedScrolls);
	}

	TravelScrollIslands(String internalName, String descriptiveName, String texture, Function<Boolean, String> description, @NonNull ServerType serverType, SkillCategories associatedSkill, Integer islandTier) {
		this.internalName = internalName;
		this.descriptiveName = descriptiveName;
		this.texture = texture;
		this.description = description;
		this.serverType = serverType;
		this.associatedSkill = associatedSkill;
		this.islandTier = islandTier;
		this.requiredSkillLevel = 0;
		this.warpMenuTitle = null;
		this.unlockScroll = null;
	}

	TravelScrollIslands(String internalName, String descriptiveName, String texture, Function<Boolean, String> description, @NonNull ServerType serverType) {
		this.internalName = internalName;
		this.descriptiveName = descriptiveName;
		this.texture = texture;
		this.description = description;
		this.serverType = serverType;
		this.associatedSkill = null;
		this.islandTier = null;
		this.requiredSkillLevel = 0;
		this.warpMenuTitle = null;
		this.unlockScroll = null;
	}

	TravelScrollIslands(String internalName, String descriptiveName, String texture, Function<Boolean, String> description, @NonNull ServerType serverType, List<TravelScrollType> associatedScrolls) {
		this.internalName = internalName;
		this.description = description;
		this.serverType = serverType;
		this.texture = texture;
		this.descriptiveName = descriptiveName;
		this.associatedScrolls.addAll(associatedScrolls);
		this.associatedSkill = null;
		this.islandTier = null;
		this.requiredSkillLevel = 0;
		this.warpMenuTitle = null;
		this.unlockScroll = null;
	}

	public Text getDescriptiveName() {
		return Text.of(descriptiveName);
	}

	public String getWarpMenuTitle() {
		return warpMenuTitle != null ? warpMenuTitle : StringUtility.toNormalCase(internalName) + " Warps";
	}

	public boolean isUnlocked(SkyBlockPlayer player) {
		if (unlockScroll != null) {
			return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.USED_SCROLLS, DatapointStringList.class)
					.getValue().contains(unlockScroll.getInternalName());
		}
		return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.VISITED_ISLANDS, DatapointStringList.class)
				.getValue().contains(internalName);
	}

	/** The message shown when the player's skill is too low to travel here, or null if they may. */
	public @Nullable String entryBlockReason(SkyBlockPlayer player) {
		if (associatedSkill == null || requiredSkillLevel <= 0) return null;
		if (player.getSkills().getCurrentLevel(associatedSkill) >= requiredSkillLevel) return null;
		return "<c>You must be at least " + StringUtility.toNormalCase(associatedSkill.name()) + " Level "
				+ StringUtility.getAsRomanNumeral(requiredSkillLevel) + " to join this island!";
	}

	public static String resolveAlias(String warp) {
		return WARP_ALIASES.getOrDefault(warp.toLowerCase(), warp);
	}

	public static @Nullable TravelScrollIslands getFromType(ServerType type) {
		for (TravelScrollIslands island : values()) {
			if (island.getServerType().equals(type)) {
				return island;
			}
		}
		return null;
	}

	public static @Nullable TravelScrollIslands getFromInternalName(String internalName) {
		for (TravelScrollIslands island : values()) {
			if (island.getInternalName().equalsIgnoreCase(internalName)) {
				return island;
			}
		}
		return null;
	}

	public static @Nullable TravelScrollIslands getFromTravelScroll(TravelScrollType type) {
		for (TravelScrollIslands island : values()) {
			if (island.getAssociatedScrolls().contains(type)) {
				return island;
			}
		}
		return null;
	}

}
