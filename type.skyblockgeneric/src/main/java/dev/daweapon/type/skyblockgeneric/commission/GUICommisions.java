package dev.daweapon.type.skyblockgeneric.commission;

import dev.daweapon.type.skyblockgeneric.hotm.HotmPowder;
import dev.daweapon.type.skyblockgeneric.hotm.HotmService;
import dev.daweapon.type.skyblockgeneric.achievement.SkyBlockAchievementSync;
import dev.daweapon.type.skyblockgeneric.accessory.AccessoryEconomyHooks;
import dev.daweapon.type.skyblockgeneric.accessory.handler.UtilityAccessories;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.gathering.GatheringPetBonuses;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.loottable.RareDropAward;
import dev.daweapon.type.generic.gui.inventory.HypixelInventoryGUI;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.inventory.item.GUIClickableItem;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointCommissions;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointHOTM;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import dev.daweapon.type.skyblockgeneric.user.pets.PetEvents;

// GUI showing a player's active commissions for one zone and letting them claim completed rewards.
public class GUICommisions extends HypixelInventoryGUI {

	private final CommissionZone zone;

	public GUICommisions() {
		this(CommissionZone.DWARVEN_MINES);
	}

	public GUICommisions(CommissionZone zone) {
		super("Commissions", InventoryType.CHEST_4_ROW);
		this.zone = zone;
	}

	@Override
	public void onOpen(InventoryGUIOpenEvent e) {
		fill(FILLER_ITEM);

		SkyBlockPlayer player = (SkyBlockPlayer) getPlayer();
		DatapointCommissions.PlayerCommissionData commissionData = Commissions.getCommissionData(player);
		commissionData.checkAndResetDaily();

		if (Commissions.needsGeneration(player, zone)) {
			Commissions.generateCommissions(player, zone);
			commissionData = Commissions.getCommissionData(player); // refresh
		}

		int slotCount = Commissions.getCommissionSlots(player);
		int[] slots = Commissions.getGuiSlots(slotCount);

		List<DatapointCommissions.ActiveCommission> activeCommissions = zone.active(commissionData);

		DatapointHOTM.PlayerHOTMData hotmData = player.getSkyblockDataHandler()
				.get(SkyBlockDataHandler.Data.HOTM, DatapointHOTM.class).getValue();
		int hotmTier = hotmData.getTier();
		boolean isHotmMaxed = hotmData.isMaxed();
		int remainingDailyBonus = commissionData.getRemainingDailyBonus();

		for (int i = 0; i < slots.length && i < activeCommissions.size(); i++) {
			int index = i;
			DatapointCommissions.ActiveCommission activeCommission = activeCommissions.get(i);
			Commission commission = Commissions.getCommissionByName(activeCommission.getCommissionName());
			boolean willGetDailyBonus = index < remainingDailyBonus;

			set(new GUIClickableItem(slots[i]) {
				@Override
				public void run(InventoryPreClickEvent e, HypixelPlayer p) {
					SkyBlockPlayer player = (SkyBlockPlayer) p;
					DatapointCommissions.PlayerCommissionData commissionData = Commissions.getCommissionData(player);
					if (activeCommission.isCompleted() && !activeCommission.isClaimed()) {
						SkyBlockDataHandler dataHandler = player.getSkyblockDataHandler();

						DatapointHOTM.PlayerHOTMData hotmData =
								dataHandler.get(SkyBlockDataHandler.Data.HOTM, DatapointHOTM.class).getValue();

						int hotmTier = hotmData.getTier();
						boolean isHotmMaxed = hotmData.isMaxed();
						boolean isDailyBonus = commissionData.hasDailyBonus();

						CommissionReward reward = zone == CommissionZone.CRYSTAL_HOLLOWS
								? CommissionReward.calculateHollows(hotmTier, isHotmMaxed, isDailyBonus)
								: commission.isGlacite()
								? CommissionReward.calculateGlacite(hotmTier, isHotmMaxed, isDailyBonus)
								: CommissionReward.calculate(hotmTier, isHotmMaxed, isDailyBonus);

						if (reward.hotmXp() > 0) {
							HotmService.addExperience(player, Math.round(PetEvents.xpGained(player, PetEvent.XpType.HOTM, null,
									reward.hotmXp() + UtilityAccessories.commissionHotmXp(player))));
						}

						if (reward.mithrilPowder() > 0) {
							HotmService.gainPowder(player, HotmPowder.MITHRIL, reward.mithrilPowder());
						}

						if (reward.glacitePowder() > 0) {
							HotmService.gainPowder(player, HotmPowder.GLACITE, reward.glacitePowder());
						}

						if (reward.gemstonePowder() > 0) {
							HotmService.gainPowder(player, HotmPowder.GEMSTONE, reward.gemstonePowder());
						}

						boolean hollows = zone == CommissionZone.CRYSTAL_HOLLOWS;
						if (HotmService.claimDailyGrind(player, hollows ? HotmPowder.GEMSTONE : HotmPowder.MITHRIL)) {
							player.sendMessage(hollows
											? "<d><b>DAILY GRIND <r><7>You earned <d>{} Gemstone Powder<7> for your first commission of the day."
											: "<2><b>DAILY GRIND <r><7>You earned <2>{} Mithril Powder<7> for your first commission of the day.",
									String.format("%,d", HotmService.dailyPowderAmount(player)));
						}

						if (reward.miningXp() > 0) {
							player.getSkills().increase(player, SkillCategories.MINING, (double) reward.miningXp());
						}

						RareDropAward.tryAward(player, ItemType.DYE_NYANZA, 1.0 / 250_000 * 100);

						activeCommission.setClaimed(true);
						commissionData.completeCommission();
						Commissions.saveCommissionData(player, commissionData);
						SkyBlockAchievementSync.syncCommissions(player);
						Commissions.replaceCommissionAt(player, index, zone);
						new GUICommisions(zone).open(player);
					}
				}

				@Override
				public ItemStack.Builder getItem(HypixelPlayer player) {
					List<Text> lore = new ArrayList<>();
					lore.add(Text.of("<7>Commissions are tasks given directly"));
					lore.add(Text.of("<7>by the king which give bountiful"));
					lore.add(Text.of("<7>rewards."));
					lore.add(Text.literal(" "));

					if (commission != null) {
						lore.add(Text.of(commission.nameColourTag() + "{}", commission.name));
						lore.add(Text.of("<7>{}", commission.generateDescription()));
					} else {
						lore.add(Text.of("<9>Unknown Commission"));
						lore.add(Text.of("<7>Complete this task for rewards."));
					}

					lore.add(Text.empty());
					lore.add(Text.of("<9>Rewards"));
					lore.addAll(zone == CommissionZone.CRYSTAL_HOLLOWS
							? CommissionReward.getHollowsRewardLore(hotmTier, isHotmMaxed, willGetDailyBonus)
							: commission.isGlacite()
							? CommissionReward.getGlaciteRewardLore(hotmTier, isHotmMaxed, willGetDailyBonus)
							: CommissionReward.getRewardLore(hotmTier, isHotmMaxed, willGetDailyBonus));

					lore.add(Text.literal(" "));

					if (activeCommission.isCompleted()) {
						lore.add(Text.of("<a><l>COMPLETED!"));
						lore.add(Text.empty());
						lore.add(Text.of("<e>Click to claim rewards!"));
					} else {
						lore.add(Text.of("<9>Progress"));
						int progress = activeCommission.getProgress();
						int target = commission != null ? commission.objective.amount : 100;
						int percentage = Math.min(100, (int) ((progress / (double) target) * 100));
						lore.add(buildProgressBar(percentage).append("<9>{}%", percentage));
					}

					return ItemStacks.item(Material.WRITABLE_BOOK, 1,
							Text.of("<6>Commission #{}", index + 1), lore);
				}
			});
		}

		set(new GUIClickableItem(30) {
			@Override
			public void run(InventoryPreClickEvent e, HypixelPlayer player) {
				new GUICommissionMilestones(zone).open(player);
			}

			@Override
			public ItemStack.Builder getItem(HypixelPlayer player) {
				DatapointCommissions.PlayerCommissionData data = Commissions.getCommissionData((SkyBlockPlayer) player);
				int totalCompleted = data.getTotalCompleted();

				List<Text> lore = new ArrayList<>();
				lore.add(Text.of("<7>View milestone progress and rewards!"));
				lore.add(Text.literal(" "));

				CommissionMilestone nextMilestone = CommissionMilestone.getNextMilestone(totalCompleted);
				if (nextMilestone != null) {
					double progress = data.getMilestoneProgress(nextMilestone.getTier());

					lore.add(Text.of("<7>Progress to milestone {:roman}: <e>{}<6>%",
							nextMilestone.getTier(), String.format("%.1f", progress)));
					lore.add(buildMilestoneProgressBar(progress).append("<e>{}<6>/<e>{}",
							totalCompleted, nextMilestone.getCommissionsRequired()));
					lore.add(Text.literal(" "));
					lore.add(Text.of("<7>Tier {:roman} Rewards:", nextMilestone.getTier()));
					for (Text reward : nextMilestone.getRewardDescriptions()) {
						lore.add(Text.of("  {}", reward));
					}
				} else {
					lore.add(Text.of("<a><l>ALL MILESTONES COMPLETED!"));
				}

				lore.add(Text.literal(" "));
				lore.add(Text.of("<e>Click to view!"));

				return ItemStacks.item(Material.FILLED_MAP, 1,
						Text.of("<e>Commission Milestones"), lore);
			}
		});

		set(GUIClickableItem.getCloseItem(31));
		updateItemStacks(getInventory(), getPlayer());
	}

	private Text buildProgressBar(int percentage) {
		int filled = percentage / 5; // 20 segments total
		int empty = 20 - filled;

		return Text.of("<l><m><f>{}<7>{} ",
				" ".repeat(Math.max(0, filled)),
				" ".repeat(Math.max(0, empty)));
	}

	private Text buildMilestoneProgressBar(double percentage) {
		int filled = (int) (percentage / 5); // 20 segments total
		int empty = 20 - filled;
		return Text.of("<l><m><2>{}<f>{} ",
				" ".repeat(Math.max(0, filled)),
				" ".repeat(Math.max(0, empty)));
	}

	@Override
	public boolean allowHotkeying() {
		return false;
	}

	@Override
	public void onBottomClick(InventoryPreClickEvent e) {

	}
}
