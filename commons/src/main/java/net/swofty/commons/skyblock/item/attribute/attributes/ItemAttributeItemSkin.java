package net.swofty.commons.skyblock.item.attribute.attributes;

import net.swofty.commons.skyblock.item.attribute.ItemAttribute;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import org.jetbrains.annotations.Nullable;

public class ItemAttributeItemSkin extends ItemAttribute<String> {
    @Override
    public String getKey() {
        return "item_skin";
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
        return getValue();
    }
}
