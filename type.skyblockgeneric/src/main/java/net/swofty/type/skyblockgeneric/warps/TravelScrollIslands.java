package net.swofty.type.skyblockgeneric.warps;

import lombok.AccessLevel;
import lombok.Getter;
import net.swofty.commons.ServerType;
import net.swofty.commons.StringUtility;
import net.swofty.type.generic.data.datapoints.DatapointStringList;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Getter
public enum TravelScrollIslands {
	PRIVATE_ISLAND("home", "<b>Private Island",
			"f151cffdaf303673531a7651b36637cad912ba485643158e548d59b2ead5011",
			(unused) -> "Your very own chunk of SkyBlock. Nice housing for your minions.", ServerType.SKYBLOCK_ISLAND),
	SKYBLOCK_HUB("hub", "<b>SkyBlock Hub", "9c465a5d348c53d473f8115ed8923be416f35149f73ebaf5f2b05e13401e814f", (unused) -> "Where everything happens and anything is possible.",
			ServerType.SKYBLOCK_HUB, List.of(TravelScrollType.HUB_MUSEUM, TravelScrollType.HUB_DARK_AUCTION, TravelScrollType.HUB_CASTLE,
					TravelScrollType.HUB_CRYPTS, TravelScrollType.HUB_WIZARD_TOWER, TravelScrollType.HUB_ELIZABETH,
					TravelScrollType.HUB_TRADE_CENTER, TravelScrollType.HUB_TAYLOR)),
	DUNGEON_HUB("dungeon_hub", "<c>Dungeon Hub", TravelScrollType.Textures.WITHER_SKELETON,
			(unused) -> "Group up with friends and take on challenging Dungeons.", ServerType.SKYBLOCK_DUNGEON_HUB),
	THE_BARN("barn", "<a>The Barn", TravelScrollType.Textures.BARN,
			(unused) -> "Gather crops and fight the animals that roam the Farming Islands.", ServerType.SKYBLOCK_THE_FARMING_ISLANDS,
			List.of(TravelScrollType.FARMING_MUSHROOM_DESERT, TravelScrollType.FARMING_TRAPPERS_DEN)),
	THE_PARK("park", "<a>The Park", TravelScrollType.Textures.PARK,
			(unused) -> "Chop down trees and explore the forests of the Park.", ServerType.SKYBLOCK_THE_PARK,
			TravelScrollType.THE_PARK, List.of(TravelScrollType.PARK_HOWLING_CAVE, TravelScrollType.PARK_JUNGLE_ISLAND)),
	GOLD_MINE("gold", "<a>Gold Mine <7>- <b>Spawn",
			"d8573ed917444316b0b28dd9927fd68e56f6625fcfa73ad80b8770d5139891b1",
			(unused) -> "Your first stop for extended mining related activities and home to SkyBlock's local janitor Rusty", ServerType.SKYBLOCK_GOLD_MINE, SkillCategories.MINING, 1),
	DEEP_CAVERNS("deep", "<a>Deep Caverns <7>- <b>Spawn",
			"74213dc6dc4b1641defd333f4a4732cc714dd677718fa10f140a6939c12aa32b",
			(unused) -> "An island that gets progressively deeper and contains 6 layers of dangerous mobs and new resources.", ServerType.SKYBLOCK_DEEP_CAVERNS, SkillCategories.MINING, 2),
	DWARVEN_MINES("dwarven", "<a>Dwarven Mines <7>- <b>Spawn",
			"6b20b23c1aa2be0270f016b4c90d6ee6b8330a17cfef87869d6ad60b2ffbf3b5",
			(unused) -> "An island that gets progressively deeper and contains 6 layers of dangerous mobs and new resources.", ServerType.SKYBLOCK_DWARVEN_MINES, SkillCategories.MINING, 2,
			List.of(TravelScrollType.DWARVEN_FORGE, TravelScrollType.DWARVEN_BASE_CAMP)),
	CRYSTAL_HOLLOWS("crystals", "<d>Crystal Hollows", TravelScrollType.Textures.SCATHA,
			(unused) -> "A vast network of caves full of gemstones, treasure and ancient secrets.", null,
			List.of(TravelScrollType.CRYSTAL_NUCLEUS)),
	SPIDERS_DEN("spider", "<c>Spider's Den", TravelScrollType.Textures.SPIDER,
			(unused) -> "A dark and dangerous island crawling with spiders.", ServerType.SKYBLOCK_SPIDERS_DEN,
			List.of(TravelScrollType.SPIDERS_DEN_NEST, TravelScrollType.SPIDERS_DEN_ARACHNE)),
	THE_END("end", "<5>The End", TravelScrollType.Textures.ENDERMAN,
			(unused) -> "Home of the Endermen and the dragons they summon.", ServerType.SKYBLOCK_THE_END,
			List.of(TravelScrollType.THE_END_DRAGONS_NEST, TravelScrollType.THE_END_VOID_SEPULTURE)),
	CRIMSON_ISLE("nether", "<a>Crimson Isle <7>- <b>Spawn",
			"64457b2d7acf522d1912f0d6c6139440ba9d333a6bf61a2e9440e7fb3788939e",
			(unused) -> "Fight challenging bosses, discover new Sea Creatures, complete epic quests, and join your favorite faction!",
			ServerType.SKYBLOCK_CRIMSON_ISLE, SkillCategories.COMBAT, 4, 22, "Crimson Isle Warps",
			TravelScrollType.CRIMSON_ISLE_SPAWN,
			List.of(TravelScrollType.CRIMSON_ISLE_FORGOTTEN_SKULL, TravelScrollType.CRIMSON_ISLE_WASTELAND,
					TravelScrollType.CRIMSON_ISLE_DRAGONTAIL, TravelScrollType.CRIMSON_ISLE_SCARLETON,
					TravelScrollType.CRIMSON_ISLE_SMOLDERING_TOMB)),
	THE_GARDEN("garden", "<a>The Garden", TravelScrollType.Textures.ROSE_DRAGON,
			(unused) -> "Your own plot of land to farm crops and host visitors.", null),
	JERRYS_WORKSHOP("jerry", "<b>Jerry's Workshop", TravelScrollType.Textures.JERRY,
			(unused) -> "Help Jerry's elves get the gifts ready during the Season of Jerry.", ServerType.SKYBLOCK_JERRYS_WORKSHOP),
	THE_RIFT("rift", "<d>The Rift", TravelScrollType.Textures.RIFT_FERRET,
			(unused) -> "A strange dimension where time and motes rule everything.", null),
	BACKWATER_BAYOU("bayou", "<3>Backwater Bayou", TravelScrollType.Textures.HERMIT_CRAB,
			(unused) -> "A sleepy swamp full of fish, junk and odd neighbours.", ServerType.SKYBLOCK_BACKWATER_BAYOU),
	MOONGLADE_MARSH("moonglade", "<2>Moonglade Marsh", TravelScrollType.Textures.FROG,
			(unused) -> "The marshes of Galatea, where the forests need your help.", ServerType.SKYBLOCK_GALATEA,
			List.of(TravelScrollType.MOONGLADE_MURKWATER_LOCH)),
	TORRHUS_CANYON("torrhus", "<6>Torrhus Canyon", TravelScrollType.Textures.SLOTH,
			(unused) -> "A sun-baked canyon full of rare critters.", null,
			List.of(TravelScrollType.TORRHUS_SPRINGS, TravelScrollType.TORRHUS_CRITTER_SAFARI)),
	LOTUS_ATOLL("lotus", "<b>Lotus Atoll", TravelScrollType.Textures.TURTLE,
			(unused) -> "A ring of islands in the open sea.", null)
	;

	private static final Map<String, String> WARP_ALIASES = Map.ofEntries(
			Map.entry("island", "home"),
			Map.entry("village", "hub"),
			Map.entry("crypt", "crypts"),
			Map.entry("tower", "wizard"), Map.entry("wiz", "wizard"), Map.entry("wizard_tower", "wizard"),
			Map.entry("dungeons", "dungeon_hub"), Map.entry("dhub", "dungeon_hub"), Map.entry("dh", "dungeon_hub"),
			Map.entry("farming", "barn"),
			Map.entry("trap", "trapper"),
			Map.entry("trees", "park"), Map.entry("foraging", "park"),
			Map.entry("howling_cave", "howl"), Map.entry("wolf_cave", "howl"),
			Map.entry("dwarves", "dwarven"), Map.entry("dmines", "dwarven"), Map.entry("mines", "dwarven"), Map.entry("deeper", "dwarven"),
			Map.entry("base", "basecamp"), Map.entry("camp", "basecamp"), Map.entry("glacite", "basecamp"),
			Map.entry("tunnel", "basecamp"), Map.entry("tunnels", "basecamp"), Map.entry("gt", "basecamp"),
			Map.entry("hollows", "crystals"), Map.entry("ch", "crystals"),
			Map.entry("cn", "nucleus"), Map.entry("nuc", "nucleus"),
			Map.entry("spiders", "spider"),
			Map.entry("top", "nest"), Map.entry("mound", "nest"),
			Map.entry("sanctuary", "arachne"),
			Map.entry("dragons", "drag"),
			Map.entry("sepulture", "void"),
			Map.entry("isle", "nether"), Map.entry("crimson", "nether"), Map.entry("ci", "nether"),
			Map.entry("skull", "kuudra"),
			Map.entry("smoldering", "smold"), Map.entry("smoldering_tomb", "smold"),
			Map.entry("winter", "jerry"), Map.entry("workshop", "jerry"),
			Map.entry("the_rift", "rift"),
			Map.entry("backwater", "bayou"),
			Map.entry("galatea", "moonglade"), Map.entry("marsh", "moonglade"),
			Map.entry("murk", "murkwater"), Map.entry("murkwater_loch", "murkwater"), Map.entry("loch", "murkwater"),
			Map.entry("canyon", "torrhus"), Map.entry("torrhus_canyon", "torrhus"),
			Map.entry("spring", "springs"), Map.entry("torrhus_springs", "springs"),
			Map.entry("safari_zone", "safari"), Map.entry("crittersafari", "safari"), Map.entry("critter_safari", "safari"),
			Map.entry("atoll", "lotus"));

	public static final String NOT_AVAILABLE_MESSAGE = "<c>This destination isn't in SkyBlock yet.";

    private final String internalName;
    @Getter(AccessLevel.NONE)
    private final String descriptiveName;
    private final String texture;
    private final Function<Boolean, String> description;
    /** Null while the island isn't in SkyBlock yet. */
    private final @Nullable ServerType serverType;
    private final List<TravelScrollType> associatedScrolls = new ArrayList<>();

	private final SkillCategories associatedSkill;
	private final Integer islandTier;
	private final int requiredSkillLevel;
	private final @Nullable String warpMenuTitle;
	/** When set, the island's own warp is unlocked by consuming this scroll rather than by visiting. */
	private final @Nullable TravelScrollType unlockScroll;

	TravelScrollIslands(String internalName, String descriptiveName, String texture, Function<Boolean, String> description,
						@Nullable ServerType serverType, SkillCategories associatedSkill, Integer islandTier, int requiredSkillLevel,
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

	TravelScrollIslands(String internalName, String descriptiveName, String texture, Function<Boolean, String> description,
						ServerType serverType, SkillCategories associatedSkill, Integer islandTier, List<TravelScrollType> associatedScrolls) {
		this(internalName, descriptiveName, texture, description, serverType, associatedSkill, islandTier, 0, null, null, associatedScrolls);
	}

	TravelScrollIslands(String internalName, String descriptiveName, String texture, Function<Boolean, String> description,
						ServerType serverType, SkillCategories associatedSkill, Integer islandTier) {
		this(internalName, descriptiveName, texture, description, serverType, associatedSkill, islandTier, List.of());
	}

	TravelScrollIslands(String internalName, String descriptiveName, String texture, Function<Boolean, String> description,
						@Nullable ServerType serverType, TravelScrollType unlockScroll, List<TravelScrollType> associatedScrolls) {
		this(internalName, descriptiveName, texture, description, serverType, null, null, 0, null, unlockScroll, associatedScrolls);
	}

	TravelScrollIslands(String internalName, String descriptiveName, String texture, Function<Boolean, String> description,
						@Nullable ServerType serverType, List<TravelScrollType> associatedScrolls) {
		this(internalName, descriptiveName, texture, description, serverType, null, null, 0, null, null, associatedScrolls);
	}

	TravelScrollIslands(String internalName, String descriptiveName, String texture, Function<Boolean, String> description,
						@Nullable ServerType serverType) {
		this(internalName, descriptiveName, texture, description, serverType, List.of());
	}

	public Text getDescriptiveName() {
		return Text.of(descriptiveName);
	}

	public String getWarpMenuTitle() {
		return warpMenuTitle != null ? warpMenuTitle : StringUtility.toNormalCase(internalName) + " Warps";
	}

	public boolean isAvailable() {
		return serverType != null;
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

	public static void unlockAll(SkyBlockPlayer player) {
		DatapointStringList visited = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.VISITED_ISLANDS, DatapointStringList.class);
		DatapointStringList scrolls = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.USED_SCROLLS, DatapointStringList.class);
		visited.setValue(withAll(visited.getValue(), Arrays.stream(values()).map(TravelScrollIslands::getInternalName).toList()));
		scrolls.setValue(withAll(scrolls.getValue(), Arrays.stream(TravelScrollType.values()).map(TravelScrollType::getInternalName).toList()));
	}

	private static List<String> withAll(List<String> current, List<String> additions) {
		List<String> merged = new ArrayList<>(current);
		additions.stream().filter(name -> !merged.contains(name)).forEach(merged::add);
		return merged;
	}

	public static String resolveAlias(String warp) {
		return WARP_ALIASES.getOrDefault(warp.toLowerCase(), warp);
	}

	public static @Nullable TravelScrollIslands getFromType(ServerType type) {
		for (TravelScrollIslands island : values()) {
			if (island.getServerType() == type) {
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
