package dev.daweapon.commons.skyblock.item.attribute.attributes;

import dev.daweapon.commons.skyblock.item.attribute.ItemAttribute;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import org.jetbrains.annotations.Nullable;

public class ItemAttributeTrapperCrestPelts extends ItemAttribute<Long> {
    @Override
    public String getKey() {
        return "trapper_crest_pelts";
    }

    @Override
    public Long getDefaultValue(@Nullable ItemStatistics defaultStatistics) {
        return 0L;
    }

    @Override
    public Long loadFromString(String string) {
        return Long.parseLong(string);
    }

    @Override
    public String saveIntoString() {
        return String.valueOf(getValue());
    }
}
