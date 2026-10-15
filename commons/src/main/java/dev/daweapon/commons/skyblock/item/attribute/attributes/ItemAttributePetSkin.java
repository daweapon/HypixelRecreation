package dev.daweapon.commons.skyblock.item.attribute.attributes;

import dev.daweapon.commons.skyblock.item.attribute.ItemAttribute;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import org.jetbrains.annotations.Nullable;

// Item attribute storing the ItemType id of the Pet Skin applied to a pet, or an empty string.
public class ItemAttributePetSkin extends ItemAttribute<String> {
    @Override
    public String getKey() {
        return "pet_skin";
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
