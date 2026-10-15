package dev.daweapon.commons.skyblock.item.attribute.attributes;

import dev.daweapon.commons.skyblock.item.attribute.ItemAttribute;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import org.jetbrains.annotations.Nullable;

// Item attribute storing a fishing rod's equipped line.
public class ItemAttributeFishingLine extends ItemAttribute<String> {
    @Override
    public String getKey() {
        return "fishing_line";
    }

    @Override
    public String getDefaultValue(@Nullable ItemStatistics defaultStatistics) {
        return "none";
    }

    @Override
    public String loadFromString(String string) {
        return string == null || string.isBlank() ? "none" : string;
    }

    @Override
    public String saveIntoString() {
        return getValue() == null || getValue().isBlank() ? "none" : getValue();
    }
}
