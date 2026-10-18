package dev.daweapon.type.skyblockgeneric.tab.widgets;

import dev.daweapon.type.skyblockgeneric.tab.TabLocation;
import dev.daweapon.type.skyblockgeneric.tab.TabWidget;
import net.minestom.server.item.Material;

import java.util.Set;

abstract class AreaWidget implements TabWidget {
    private final String id;
    private final String displayName;
    private final Material icon;
    private final int priority;
    private final Set<TabLocation> locations;

    AreaWidget(String id, String displayName, Material icon, int priority, TabLocation... locations) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        this.priority = priority;
        this.locations = Set.of(locations);
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String displayName() {
        return displayName;
    }

    @Override
    public Material icon() {
        return icon;
    }

    @Override
    public int defaultPriority() {
        return priority;
    }

    @Override
    public Set<TabLocation> locations() {
        return locations;
    }
}
