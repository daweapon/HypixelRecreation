package dev.daweapon.commons.skyblock.item.attribute.attributes;

import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.commons.skyblock.item.attribute.ItemAttribute;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import org.jetbrains.annotations.Nullable;

// Item attribute storing an item's rarity tier.
public class ItemAttributeRarity extends ItemAttribute<Rarity> {
    @Override
    public String getKey() {
        return "rarity";
    }

    @Override
    public Rarity getDefaultValue(@Nullable ItemStatistics defaultStatistics) {
        return Rarity.COMMON;
    }

    @Override
    public Rarity loadFromString(String string) {
        return Rarity.valueOf(string);
    }

    @Override
    public String saveIntoString() {
        return this.value.name();
    }
}
