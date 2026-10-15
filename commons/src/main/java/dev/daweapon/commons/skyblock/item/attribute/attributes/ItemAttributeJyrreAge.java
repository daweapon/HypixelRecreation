package dev.daweapon.commons.skyblock.item.attribute.attributes;

import dev.daweapon.commons.skyblock.item.attribute.ItemAttribute;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import org.jetbrains.annotations.Nullable;

public class ItemAttributeJyrreAge extends ItemAttribute<Long> {
    @Override
    public String getKey() {
        return "jyrre_age_minutes";
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
