package net.swofty.commons.skyblock.item.attribute.attributes;

import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.commons.skyblock.item.attribute.ItemAttribute;
import org.jetbrains.annotations.Nullable;

// Item attribute storing the ItemType id of the Pet Item a pet is holding, or an empty string.
public class ItemAttributePetHeldItem extends ItemAttribute<String> {
    @Override
    public String getKey() {
        return "pet_held_item";
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
