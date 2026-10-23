package dev.daweapon.commons.skyblock.item.attribute.attributes;

import dev.daweapon.commons.skyblock.item.attribute.ItemAttribute;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import org.jetbrains.annotations.Nullable;

public class ItemAttributeCrestKills extends ItemAttribute<Long> {
	@Override
	public String getKey() {
		return "crest_kills";
	}

	@Override
	public Long getDefaultValue(@Nullable ItemStatistics defaultStatistics) {
		return 0L;
	}

	@Override
	public Long loadFromString(String string) {
		try {
			return Long.parseLong(string);
		} catch (NumberFormatException e) {
			return 0L;
		}
	}

	@Override
	public String saveIntoString() {
		return String.valueOf(getValue());
	}
}
