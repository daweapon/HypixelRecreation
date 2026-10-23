package dev.daweapon.commons.skyblock.item.attribute.attributes;

import dev.daweapon.commons.skyblock.item.attribute.ItemAttribute;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import org.jetbrains.annotations.Nullable;

public class ItemAttributeArtOfWar extends ItemAttribute<Integer> {
	@Override
	public String getKey() {
		return "art_of_war";
	}

	@Override
	public Integer getDefaultValue(@Nullable ItemStatistics defaultStatistics) {
		return 0;
	}

	@Override
	public Integer loadFromString(String string) {
		try {
			return Integer.parseInt(string);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	@Override
	public String saveIntoString() {
		return String.valueOf(getValue());
	}
}
