package dev.daweapon.commons.skyblock.item.attribute.attributes;

import dev.daweapon.commons.skyblock.item.attribute.ItemAttribute;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import org.jetbrains.annotations.Nullable;

// Item attribute flagging whether an item has been infused with Mithril.
public class ItemAttributeMithrilInfusion extends ItemAttribute<Boolean> {
    @Override
    public String getKey() {
        return "mithril_infusion";
    }

    @Override
    public Boolean getDefaultValue(@Nullable ItemStatistics defaultStatistics) {
        return false;
    }

    @Override
    public Boolean loadFromString(String string) {
        return Boolean.parseBoolean(string);
    }

    @Override
    public String saveIntoString() {
        return this.value.toString();
    }
}
