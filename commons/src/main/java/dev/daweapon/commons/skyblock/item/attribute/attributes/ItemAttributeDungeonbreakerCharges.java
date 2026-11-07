package dev.daweapon.commons.skyblock.item.attribute.attributes;

import dev.daweapon.commons.skyblock.item.attribute.ItemAttribute;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import org.jetbrains.annotations.Nullable;

public class ItemAttributeDungeonbreakerCharges extends ItemAttribute<String> {
	@Override
	public String getKey() {
		return "dungeonbreaker_charges";
	}

	@Override
	public String getDefaultValue(@Nullable ItemStatistics defaultStatistics) {
		return "";
	}

	@Override
	public String loadFromString(String string) {
		return string == null ? "" : string;
	}

	@Override
	public String saveIntoString() {
		return getValue() == null ? "" : getValue();
	}
}
