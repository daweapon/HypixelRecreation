package net.swofty.type.dwarvenmines.events;

import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.item.Material;
import net.swofty.type.skyblockgeneric.commission.CommissionProgress;
import net.swofty.type.skyblockgeneric.commission.Objective;
import net.swofty.type.skyblockgeneric.commission.ObjectiveType;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.glacite.GlaciteOre;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.region.SkyBlockRegion;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Set;

// Tracks block-break events in the Dwarven Mines and updates a player's active commission progress.
public class ActionPlayerCommissions implements HypixelEventClass {

	private static final Set<Material> MITHRIL_BLOCKS = Set.of(Material.LIGHT_BLUE_WOOL, Material.GRAY_WOOL, Material.CYAN_TERRACOTTA, Material.PRISMARINE, Material.DARK_PRISMARINE, Material.PRISMARINE_BRICKS);

	private static final Set<Material> TITANIUM_BLOCKS = Set.of(Material.POLISHED_DIORITE);

	@PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
	public void run(PlayerBlockBreakEvent event) {
		if (!(event.getPlayer() instanceof SkyBlockPlayer player)) return;

		Material brokenMaterial = event.getBlock().material();
		if (brokenMaterial == null) return;

		SkyBlockRegion region = player.getRegion();
		RegionType regionType = region != null ? region.getType() : null;

		Objective.BlockTarget target = null;
		GlaciteOre glaciteOre = GlaciteOre.of(brokenMaterial, regionType);
		if (glaciteOre != null) {
			target = Objective.BlockTarget.valueOf(glaciteOre.name());
		} else if (MITHRIL_BLOCKS.contains(brokenMaterial)) {
			target = Objective.BlockTarget.MITHRIL;
		} else if (TITANIUM_BLOCKS.contains(brokenMaterial)) {
			target = Objective.BlockTarget.TITANIUM;
		}

		if (target == null) return;

		updateCommissionProgress(player, ObjectiveType.MINE, target, 1, regionType);
	}

	public static void updateCommissionProgress(SkyBlockPlayer player, ObjectiveType type, Objective.BlockTarget target, int amount, RegionType regionType) {
		CommissionProgress.update(player, type, target, amount, regionType);
	}
}
