package net.swofty.commons.skyblock.item.attribute.attributes;

import net.swofty.commons.skyblock.item.attribute.ItemAttribute;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import org.jetbrains.annotations.Nullable;

public class ItemAttributeSnowballAmmo extends ItemAttribute<Integer> {
    public static final int UNSET = -1;

    @Override
    public String getKey() {
        return "ammo";
    }

    @Override
    public Integer getDefaultValue(@Nullable ItemStatistics defaultStatistics) {
        return UNSET;
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
