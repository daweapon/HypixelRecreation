package net.swofty.type.skyblockgeneric.commission;

import net.swofty.type.skyblockgeneric.hotm.HotmService;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointCommissions;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

// Holds the pools of available Dwarven Mines and Crystal Hollows commissions and manages generating/replacing a player's active ones.
public class Commissions {
	public static List<Commission> CONSISTENT = List.of(

			new Commission("Mithril Miner", CommissionCategory.CONSISTENT,
					new Objective(ObjectiveType.MINE, Objective.BlockTarget.MITHRIL, 350, LocationSelector.any(), EventType.NONE),
					false
			),

			new Commission("Lava Springs Mithril", CommissionCategory.CONSISTENT,
					new Objective(ObjectiveType.MINE, Objective.BlockTarget.MITHRIL, 250, LocationSelector.of(RegionType.LAVA_SPRINGS), EventType.NONE),
					false
			),

			new Commission("Royal Mines Mithril", CommissionCategory.CONSISTENT,
					new Objective(ObjectiveType.MINE, Objective.BlockTarget.MITHRIL, 250, LocationSelector.of(RegionType.ROYAL_MINES), EventType.NONE),
					false
			),

			new Commission("Cliffside Veins Mithril", CommissionCategory.CONSISTENT,
					new Objective(ObjectiveType.MINE, Objective.BlockTarget.MITHRIL, 250, LocationSelector.of(RegionType.CLIFFSIDE_VEINS), EventType.NONE),
					false
			),

			new Commission("Rampart's Quarry Mithril", CommissionCategory.CONSISTENT,
					new Objective(ObjectiveType.MINE, Objective.BlockTarget.MITHRIL, 250, LocationSelector.of(RegionType.RAMPARTS_QUARRY), EventType.NONE),
					false
			),

			new Commission("Upper Mines Mithril", CommissionCategory.CONSISTENT,
					new Objective(ObjectiveType.MINE, Objective.BlockTarget.MITHRIL, 250, LocationSelector.of(RegionType.UPPER_MINES), EventType.NONE),
					false
			),

			new Commission("Titanium Miner", CommissionCategory.CONSISTENT,
					new Objective(ObjectiveType.MINE, Objective.BlockTarget.TITANIUM, 15, LocationSelector.any(), EventType.NONE),
					false
			),

			new Commission("Lava Springs Titanium", CommissionCategory.CONSISTENT,
					new Objective(ObjectiveType.MINE, Objective.BlockTarget.TITANIUM, 10, LocationSelector.of(RegionType.LAVA_SPRINGS), EventType.NONE),
					false
			),

			new Commission("Royal Mines Titanium", CommissionCategory.CONSISTENT,
					new Objective(ObjectiveType.MINE, Objective.BlockTarget.TITANIUM, 10, LocationSelector.of(RegionType.ROYAL_MINES), EventType.NONE),
					false
			),

			new Commission("Cliffside Veins Titanium", CommissionCategory.CONSISTENT,
					new Objective(ObjectiveType.MINE, Objective.BlockTarget.TITANIUM, 10, LocationSelector.of(RegionType.CLIFFSIDE_VEINS), EventType.NONE),
					false
			),

			new Commission("Rampart's Quarry Titanium", CommissionCategory.CONSISTENT,
					new Objective(ObjectiveType.MINE, Objective.BlockTarget.TITANIUM, 10, LocationSelector.of(RegionType.RAMPARTS_QUARRY), EventType.NONE),
					false
			),

			new Commission("Upper Mines Titanium", CommissionCategory.CONSISTENT,
					new Objective(ObjectiveType.MINE, Objective.BlockTarget.TITANIUM, 10, LocationSelector.of(RegionType.UPPER_MINES), EventType.NONE),
					false
			),

			new Commission("Goblin Slayer", CommissionCategory.CONSISTENT,
					new Objective(ObjectiveType.SLAY, Objective.BlockTarget.GOBLIN, 100, LocationSelector.any(), EventType.NONE),
					false
			),

			new Commission("Glacite Walker Slayer", CommissionCategory.CONSISTENT,
					new Objective(ObjectiveType.SLAY, Objective.BlockTarget.GLACITE_WALKER, 50, LocationSelector.any(), EventType.NONE),
					false
			),

			new Commission("Treasure Hoarder Puncher", CommissionCategory.CONSISTENT,
					new Objective(ObjectiveType.DAMAGE, Objective.BlockTarget.TREASURE_HOARDER, 10, LocationSelector.any(), EventType.NONE),
					false
			)
	);

	public static List<Commission> GLACITE_TUNNELS = List.of(
			new Commission("Umber Collector", CommissionCategory.CONSISTENT,
					new Objective(ObjectiveType.MINE, Objective.BlockTarget.UMBER, 1500, LocationSelector.glaciteTunnels(), EventType.NONE),
					false
			),

			new Commission("Tungsten Collector", CommissionCategory.CONSISTENT,
					new Objective(ObjectiveType.MINE, Objective.BlockTarget.TUNGSTEN, 1500, LocationSelector.glaciteTunnels(), EventType.NONE),
					false
			),

			new Commission("Glacite Collector", CommissionCategory.CONSISTENT,
					new Objective(ObjectiveType.MINE, Objective.BlockTarget.GLACITE, 1500, LocationSelector.glaciteTunnels(), EventType.NONE),
					false
			),

			new Commission("Aquamarine Collector", CommissionCategory.CONSISTENT,
					new Objective(ObjectiveType.MINE, Objective.BlockTarget.AQUAMARINE, 1500, LocationSelector.glaciteTunnels(), EventType.NONE),
					false
			),

			new Commission("Citrine Collector", CommissionCategory.CONSISTENT,
					new Objective(ObjectiveType.MINE, Objective.BlockTarget.CITRINE, 1500, LocationSelector.glaciteTunnels(), EventType.NONE),
					false
			),

			new Commission("Onyx Collector", CommissionCategory.CONSISTENT,
					new Objective(ObjectiveType.MINE, Objective.BlockTarget.ONYX, 1500, LocationSelector.glaciteTunnels(), EventType.NONE),
					false
			),

			new Commission("Peridot Collector", CommissionCategory.CONSISTENT,
					new Objective(ObjectiveType.MINE, Objective.BlockTarget.PERIDOT, 1500, LocationSelector.glaciteTunnels(), EventType.NONE),
					false
			)
	);

	public static List<Commission> HOLLOWS = List.of(
			hollows("Amber Gemstone Collector", ObjectiveType.COLLECT, Objective.BlockTarget.GEM_AMBER, 1000, null, false),
			hollows("Amethyst Gemstone Collector", ObjectiveType.COLLECT, Objective.BlockTarget.GEM_AMETHYST, 1000, null, false),
			hollows("Jade Gemstone Collector", ObjectiveType.COLLECT, Objective.BlockTarget.GEM_JADE, 1000, null, false),
			hollows("Ruby Gemstone Collector", ObjectiveType.COLLECT, Objective.BlockTarget.GEM_RUBY, 1000, null, false),
			hollows("Sapphire Gemstone Collector", ObjectiveType.COLLECT, Objective.BlockTarget.GEM_SAPPHIRE, 1000, null, false),
			hollows("Topaz Gemstone Collector", ObjectiveType.COLLECT, Objective.BlockTarget.GEM_TOPAZ, 1000, null, false),
			hollows("Automaton Slayer", ObjectiveType.SLAY, Objective.BlockTarget.HOLLOWS_AUTOMATON, 13, RegionType.PRECURSOR_REMNANTS, false),
			hollows("Sludge Slayer", ObjectiveType.SLAY, Objective.BlockTarget.HOLLOWS_SLUDGE, 25, RegionType.JUNGLE, false),
			hollows("Team Treasurite Member Slayer", ObjectiveType.SLAY, Objective.BlockTarget.HOLLOWS_TREASURITE_MEMBER, 13, RegionType.MITHRIL_DEPOSITS, false),
			hollows("Hollows Goblin Slayer", ObjectiveType.SLAY, Objective.BlockTarget.HOLLOWS_GOBLIN, 13, RegionType.GOBLIN_HOLDOUT, false),
			hollows("Yog Slayer", ObjectiveType.SLAY, Objective.BlockTarget.HOLLOWS_YOG, 13, RegionType.MAGMA_FIELDS, false),
			hollows("Chest Looter", ObjectiveType.OPEN, Objective.BlockTarget.CHEST, 3, null, false),
			hollows("Thyst Slayer", ObjectiveType.SLAY, Objective.BlockTarget.HOLLOWS_THYST, 5, RegionType.JUNGLE, false),
			hollows("Hard Stone Miner", ObjectiveType.MINE, Objective.BlockTarget.HARD_STONE, 1000, null, false),
			hollows("Boss Corleone Slayer", ObjectiveType.SLAY, Objective.BlockTarget.HOLLOWS_BOSS_CORLEONE, 1, RegionType.MITHRIL_DEPOSITS, true),
			hollows("Crystal Hunter", ObjectiveType.COLLECT, Objective.BlockTarget.HOLLOWS_CRYSTAL, 1, null, true)
	);

	private static Commission hollows(String name, ObjectiveType type, Objective.BlockTarget target, int amount,
									 RegionType region, boolean crystalGroup) {
		return new Commission(name, CommissionCategory.CONSISTENT,
				new Objective(type, target, amount, region == null ? LocationSelector.any() : LocationSelector.of(region), EventType.NONE),
				false, CommissionZone.CRYSTAL_HOLLOWS, crystalGroup);
	}

	public static List<Commission> SITUATIONAL = List.of(
			new Commission("Goblin Raid Slayer", CommissionCategory.SITUATIONAL,
					new Objective(ObjectiveType.SLAY, Objective.BlockTarget.GOBLIN, 20, LocationSelector.any(), EventType.GOBLIN_RAID),
					false
			),

			new Commission("Goblin Raid", CommissionCategory.SITUATIONAL,
					new Objective(ObjectiveType.PARTICIPATE, Objective.BlockTarget.NONE, 1, LocationSelector.any(), EventType.GOBLIN_RAID),
					false
			),

			new Commission("Raffle", CommissionCategory.SITUATIONAL,
					new Objective(ObjectiveType.PARTICIPATE, Objective.BlockTarget.NONE, 1, LocationSelector.any(), EventType.RAFFLE),
					false
			),

			new Commission("Golden Goblin Slayer", CommissionCategory.SITUATIONAL,
					new Objective(ObjectiveType.SLAY, Objective.BlockTarget.GOLDEN_GOBLIN, 1, LocationSelector.any(), EventType.NONE),
					false
			),

			new Commission("Star Sentry Puncher", CommissionCategory.SITUATIONAL,
					new Objective(ObjectiveType.DAMAGE, Objective.BlockTarget.STAR_SENTRY, 10, LocationSelector.any(), EventType.NONE),
					false
			),

			new Commission("Lucky Raffle", CommissionCategory.SITUATIONAL,
					new Objective(ObjectiveType.DEPOSIT, Objective.BlockTarget.NONE, 20, LocationSelector.any(), EventType.RAFFLE),
					false
			),

			new Commission("2x Mithril Powder Collector", CommissionCategory.SITUATIONAL,
					new Objective(ObjectiveType.COLLECT, Objective.BlockTarget.MITHRIL, 500, LocationSelector.any(), EventType.DOUBLE_POWDER),
					false
			)
	);

	public static List<Commission> TUTORIAL = List.of(

			new Commission("Mithril Miner", CommissionCategory.TUTORIAL,
					new Objective(ObjectiveType.MINE, Objective.BlockTarget.MITHRIL, 50, LocationSelector.any(), EventType.NONE),
					true
			),

			new Commission("First Event", CommissionCategory.TUTORIAL,
					new Objective(ObjectiveType.PARTICIPATE,  Objective.BlockTarget.NONE, 1, LocationSelector.any(), EventType.NONE),
					true
			),

			new Commission("Titanium Miner", CommissionCategory.TUTORIAL,
					new Objective(ObjectiveType.MINE, Objective.BlockTarget.TITANIUM, 2, LocationSelector.any(), EventType.NONE),
					true
			),

			new Commission("Mithril Miner II", CommissionCategory.TUTORIAL,
					new Objective(ObjectiveType.MINE, Objective.BlockTarget.MITHRIL, 150, LocationSelector.any(), EventType.NONE),
					true
			)
	);

	public static int getCommissionSlots(SkyBlockPlayer player) {
		DatapointCommissions.PlayerCommissionData data = getCommissionData(player);
		return data.getCommissionSlots() + HotmService.bonusCommissionSlots(player);
	}

	public static DatapointCommissions.PlayerCommissionData getCommissionData(SkyBlockPlayer player) {
		SkyBlockDataHandler dataHandler = player.getSkyblockDataHandler();
		return dataHandler.get(SkyBlockDataHandler.Data.COMMISSIONS, DatapointCommissions.class).getValue();
	}

	public static void saveCommissionData(SkyBlockPlayer player, DatapointCommissions.PlayerCommissionData data) {
		SkyBlockDataHandler dataHandler = player.getSkyblockDataHandler();
		dataHandler.get(SkyBlockDataHandler.Data.COMMISSIONS, DatapointCommissions.class).setValue(data);
	}

	public static void generateCommissions(SkyBlockPlayer player) {
		generateCommissions(player, CommissionZone.DWARVEN_MINES);
	}

	public static void generateCommissions(SkyBlockPlayer player, CommissionZone zone) {
		DatapointCommissions.PlayerCommissionData data = getCommissionData(player);
		List<DatapointCommissions.ActiveCommission> active = zone.active(data);

		active.clear();
		int slots = getCommissionSlots(player);
		List<String> usedNames = new ArrayList<>();

		for (int i = 0; i < slots; i++) {
			Commission commission;

			commission = pick(player, zone, i, usedNames);

			if (commission != null) {
				usedNames.add(commission.name);
				active.add(new DatapointCommissions.ActiveCommission(commission.name));
			}
		}

		saveCommissionData(player, data);
	}

	private static Commission pick(SkyBlockPlayer player, CommissionZone zone, int slotIndex, List<String> usedNames) {
		if (zone == CommissionZone.CRYSTAL_HOLLOWS) {
			List<Commission> pool = slotIndex % 2 == 0 ? hollowsNonCrystalPool() : HOLLOWS;
			Commission picked = selectRandomCommission(pool, usedNames);
			return picked != null ? picked : selectRandomCommission(HOLLOWS, usedNames);
		}
		if (slotIndex % 2 == 0) {
			return selectRandomCommission(consistentPool(player), usedNames);
		}
		Commission picked = selectRandomCommission(SITUATIONAL, usedNames);
		return picked != null ? picked : selectRandomCommission(consistentPool(player), usedNames);
	}

	private static List<Commission> hollowsNonCrystalPool() {
		return HOLLOWS.stream().filter(commission -> !commission.crystalGroup).toList();
	}

	public static List<Commission> consistentPool(SkyBlockPlayer player) {
		List<Commission> pool = new ArrayList<>(CONSISTENT);
		int tier = HotmService.data(player).getTier();
		for (Commission commission : GLACITE_TUNNELS) {
			if (tier >= commission.requiredHotmTier()) pool.add(commission);
		}
		return pool;
	}

	private static Commission selectRandomCommission(List<Commission> pool, List<String> usedNames) {
		List<Commission> available = new ArrayList<>();
		for (Commission commission : pool) {
			if (!usedNames.contains(commission.name)) {
				available.add(commission);
			}
		}

		if (available.isEmpty()) {
			return null;
		}

		return available.get(ThreadLocalRandom.current().nextInt(available.size()));
	}

	public static Commission getCommissionByName(String name) {
		for (Commission c : CONSISTENT) {
			if (c.name.equals(name)) return c;
		}
		for (Commission c : SITUATIONAL) {
			if (c.name.equals(name)) return c;
		}
		for (Commission c : GLACITE_TUNNELS) {
			if (c.name.equals(name)) return c;
		}
		for (Commission c : TUTORIAL) {
			if (c.name.equals(name)) return c;
		}
		for (Commission c : HOLLOWS) {
			if (c.name.equals(name)) return c;
		}
		return null;
	}

	public static boolean needsGeneration(SkyBlockPlayer player) {
		return needsGeneration(player, CommissionZone.DWARVEN_MINES);
	}

	public static boolean needsGeneration(SkyBlockPlayer player, CommissionZone zone) {
		DatapointCommissions.PlayerCommissionData data = getCommissionData(player);
		List<DatapointCommissions.ActiveCommission> active = zone.active(data);
		if (active.isEmpty()) {
			return true;
		}

		boolean allCompleted = true;
		for (DatapointCommissions.ActiveCommission ac : active) {
			if (!ac.isCompleted()) {
				allCompleted = false;
				break;
			}
		}

		return allCompleted;
	}

	public static int[] getGuiSlots(int slotCount) {
		return switch (slotCount) {
			case 2 -> new int[]{12, 14};
			case 3 -> new int[]{11, 13, 15};
			case 4 -> new int[]{11, 12, 14, 15};
			default -> new int[]{};
		};
	}

	@Deprecated
	public static int getCommissionLevel(SkyBlockPlayer player) {
		return getCommissionSlots(player);
	}

	public static void replaceCommissionAt(SkyBlockPlayer player, int slotIndex) {
		replaceCommissionAt(player, slotIndex, CommissionZone.DWARVEN_MINES);
	}

	public static void replaceCommissionAt(SkyBlockPlayer player, int slotIndex, CommissionZone zone) {
		DatapointCommissions.PlayerCommissionData data = getCommissionData(player);
		List<DatapointCommissions.ActiveCommission> active = zone.active(data);
		if (slotIndex < 0 || slotIndex >= active.size()) return;

		List<String> usedNames = new ArrayList<>();
		for (DatapointCommissions.ActiveCommission ac : active) {
			if (ac != null && ac.getCommissionName() != null) usedNames.add(ac.getCommissionName());
		}

		Commission newComm = pick(player, zone, slotIndex, usedNames);

		if (newComm == null) {
			active.remove(slotIndex);
		} else {
			active.set(slotIndex, new DatapointCommissions.ActiveCommission(newComm.name));
		}

		saveCommissionData(player, data);
	}

}
