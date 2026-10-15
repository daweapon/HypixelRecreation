package dev.daweapon.type.skyblockgeneric.item.set.sets;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.item.set.impl.ArmorSet;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class EmeraldArmorSet implements ArmorSet {
	public static final int EMERALDS_PER_POINT = 3_000;
	public static final int MAX_BONUS = 350;

	public static double bonus(long emeraldsCollected) {
		return Math.min(MAX_BONUS, Math.max(0, emeraldsCollected) / EMERALDS_PER_POINT);
	}

	@Override
	public String getName() {
		return "Tank";
	}

	@Override
	public ArrayList<String> getDescription() {
		return new ArrayList<>(List.of(
				"<7>Increases <c>❤ Health <7>by <a>+1 <7>and",
				"<a>❈ Defense <7>by <a>+1 <7>for every <a>3,000",
				"<7>Emeralds in your collection. Max <a>350",
				"<7>each."));
	}

	@Override
	public ItemStatistics getStatistics(SkyBlockPlayer player) {
		double bonus = bonus(player.getCollection().get(ItemType.EMERALD));
		return ItemStatistics.builder()
				.withBase(ItemStatistic.HEALTH, bonus)
				.withBase(ItemStatistic.DEFENSE, bonus)
				.build();
	}
}
