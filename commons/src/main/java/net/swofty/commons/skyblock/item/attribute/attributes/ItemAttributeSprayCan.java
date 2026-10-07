package net.swofty.commons.skyblock.item.attribute.attributes;

import net.swofty.commons.skyblock.item.attribute.ItemAttribute;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import org.jetbrains.annotations.Nullable;

// Item attribute storing a Spray Can's ink levels and selected spray, as JSON.
public class ItemAttributeSprayCan extends ItemAttribute<String> {
    @Override
    public String getKey() {
        return "spray_can";
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
