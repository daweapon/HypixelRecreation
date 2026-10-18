package dev.daweapon.type.skyblockgeneric.tab;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.Material;

import java.util.List;
import java.util.Set;

public interface TabWidget {

    String id();

    String displayName();

    Material icon();

    int defaultPriority();

    List<Text> lines(SkyBlockPlayer player, TabWidgetContext context);

    default boolean otherSection() {
        return false;
    }

    default String description() {
        return "Shows " + displayName().toLowerCase() + " information on your tab list.";
    }

    default boolean alwaysOn() {
        return false;
    }

    default Set<TabLocation> locations() {
        return Set.of();
    }

    default boolean enabledByDefault(TabLocation location) {
        return true;
    }

    default List<TabWidgetToggle> toggles() {
        return List.of();
    }

    default boolean availableAt(TabLocation location) {
        return locations().isEmpty() || locations().contains(location);
    }
}
