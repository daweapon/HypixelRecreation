package dev.daweapon.commons.skyblock.item.attribute.attributes;

import dev.daweapon.commons.skyblock.item.attribute.ItemAttribute;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

// Item attribute storing which Necron's Blade Scrolls have been applied to this specific item
// instance, in application order (so the most-recently-applied scroll is derivable).
public class ItemAttributeAppliedScrolls extends ItemAttribute<List<String>> {

    @Override
    public String getKey() {
        return "applied_scrolls";
    }

    @Override
    public List<String> getDefaultValue(@Nullable ItemStatistics defaultStatistics) {
        return new ArrayList<>();
    }

    @Override
    public List<String> loadFromString(String string) {
        List<String> scrolls = new ArrayList<>();
        if (string.isEmpty()) {
            return scrolls;
        }

        scrolls.addAll(List.of(string.split(",")));
        return scrolls;
    }

    @Override
    public String saveIntoString() {
        return String.join(",", this.value);
    }
}
