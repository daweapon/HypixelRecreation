package net.swofty.commons.skyblock.item.attribute.attributes;

import net.swofty.commons.skyblock.item.attribute.ItemAttribute;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import org.jetbrains.annotations.Nullable;

public class ItemAttributePetBulwarkKills extends ItemAttribute<Integer> {
    @Override
    public String getKey() {
        return "pet_bulwark_kills";
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
