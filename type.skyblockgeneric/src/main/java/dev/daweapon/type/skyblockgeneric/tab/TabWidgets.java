package dev.daweapon.type.skyblockgeneric.tab;

import dev.daweapon.type.skyblockgeneric.tab.widgets.AreaWidgets;
import dev.daweapon.type.skyblockgeneric.tab.widgets.CoreWidgets;
import dev.daweapon.type.skyblockgeneric.tab.widgets.EconomyWidgets;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class TabWidgets {
    private static final List<TabWidget> ALL = build();

    private TabWidgets() {
    }

    private static List<TabWidget> build() {
        List<TabWidget> widgets = new ArrayList<>();
        widgets.addAll(CoreWidgets.all());
        widgets.addAll(EconomyWidgets.all());
        widgets.addAll(AreaWidgets.all());
        return List.copyOf(widgets);
    }

    public static List<TabWidget> all() {
        return ALL;
    }

    public static Optional<TabWidget> byId(String id) {
        return ALL.stream().filter(widget -> widget.id().equals(id)).findFirst();
    }
}
