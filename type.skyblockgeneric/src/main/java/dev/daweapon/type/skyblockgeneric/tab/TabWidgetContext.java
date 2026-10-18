package dev.daweapon.type.skyblockgeneric.tab;

public record TabWidgetContext(TabLocation location, TabSettings settings) {

    public boolean shows(TabWidget widget, String toggleId) {
        return settings.shows(location, widget, toggleId);
    }

    public boolean chosen(TabWidget widget, String toggleId) {
        return settings.toggleValue(location, widget, toggleId);
    }
}
