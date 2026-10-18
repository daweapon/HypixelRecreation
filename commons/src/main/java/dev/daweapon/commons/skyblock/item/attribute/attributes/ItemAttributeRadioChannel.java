package dev.daweapon.commons.skyblock.item.attribute.attributes;

import dev.daweapon.commons.skyblock.item.attribute.ItemAttribute;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import org.jetbrains.annotations.Nullable;

public class ItemAttributeRadioChannel extends ItemAttribute<Integer> {
    @Override
    public String getKey() {
        return "radio_channel";
    }

    @Override
    public Integer getDefaultValue(@Nullable ItemStatistics defaultStatistics) {
        return 0;
    }

    @Override
    public Integer loadFromString(String string) {
        return Integer.parseInt(string);
    }

    @Override
    public String saveIntoString() {
        return String.valueOf(getValue());
    }
}
