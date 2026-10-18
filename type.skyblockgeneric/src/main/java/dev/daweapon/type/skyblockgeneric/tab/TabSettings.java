package dev.daweapon.type.skyblockgeneric.tab;

import dev.daweapon.type.generic.data.HypixelDataHandler;
import dev.daweapon.type.generic.data.datapoints.DatapointString;
import dev.daweapon.type.generic.user.HypixelPlayer;
import org.json.JSONObject;

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public final class TabSettings {
    private static final String PLAYER_LIST_INFO = "pli";
    private static final String WIDGETS = "w";

    private final JSONObject root;
    private final Consumer<String> sink;

    public TabSettings(String raw, Consumer<String> sink) {
        this.root = parse(raw);
        this.sink = sink;
    }

    public static TabSettings of(HypixelPlayer player) {
        DatapointString datapoint = player.getDataHandler()
                .get(HypixelDataHandler.Data.TAB_WIDGET_SETTINGS, DatapointString.class);
        return new TabSettings(datapoint.getValue(), datapoint::setValue);
    }

    private static JSONObject parse(String raw) {
        if (raw == null || raw.isBlank()) return new JSONObject();
        try {
            return new JSONObject(raw);
        } catch (RuntimeException ignored) {
            return new JSONObject();
        }
    }

    public boolean playerListInfo() {
        return root.optBoolean(PLAYER_LIST_INFO, true);
    }

    public void setPlayerListInfo(boolean enabled) {
        root.put(PLAYER_LIST_INFO, enabled);
        persist();
    }

    public boolean enabled(TabLocation location, TabWidget widget) {
        if (widget.unavailableReason() != null) return false;
        if (widget.alwaysOn()) return true;
        return entry(location, widget).optBoolean("e", widget.enabledByDefault(location));
    }

    public int priority(TabLocation location, TabWidget widget) {
        return entry(location, widget).optInt("p", widget.defaultPriority());
    }

    public boolean spacing(TabLocation location, TabWidget widget) {
        return entry(location, widget).optBoolean("s", true);
    }

    public boolean wrapping(TabLocation location, TabWidget widget) {
        return entry(location, widget).optBoolean("w", false);
    }

    public boolean minimized(TabLocation location, TabWidget widget) {
        return entry(location, widget).optBoolean("m", false);
    }

    public boolean shows(TabLocation location, TabWidget widget, String toggleId) {
        return !minimized(location, widget) || toggleValue(location, widget, toggleId);
    }

    public boolean toggleValue(TabLocation location, TabWidget widget, String toggleId) {
        boolean fallback = widget.toggles().stream()
                .filter(toggle -> toggle.id().equals(toggleId))
                .findFirst()
                .map(TabWidgetToggle::defaultShown)
                .orElse(true);
        JSONObject toggles = entry(location, widget).optJSONObject("t");
        return toggles == null ? fallback : toggles.optBoolean(toggleId, fallback);
    }

    public void setEnabled(TabLocation location, TabWidget widget, boolean value) {
        write(location, widget, "e", value);
    }

    public void setSpacing(TabLocation location, TabWidget widget, boolean value) {
        write(location, widget, "s", value);
    }

    public void setWrapping(TabLocation location, TabWidget widget, boolean value) {
        write(location, widget, "w", value);
    }

    public void setMinimized(TabLocation location, TabWidget widget, boolean value) {
        write(location, widget, "m", value);
    }

    public void setToggle(TabLocation location, TabWidget widget, String toggleId, boolean value) {
        JSONObject entry = mutableEntry(location, widget);
        JSONObject toggles = entry.optJSONObject("t");
        if (toggles == null) {
            toggles = new JSONObject();
            entry.put("t", toggles);
        }
        toggles.put(toggleId, value);
        persist();
    }

    public List<TabWidget> available(TabLocation location) {
        return TabWidgets.all().stream()
                .filter(widget -> widget.availableAt(location))
                .sorted(Comparator.comparingInt((TabWidget widget) -> priority(location, widget))
                        .thenComparing(TabWidget::id))
                .toList();
    }

    public List<TabWidget> active(TabLocation location) {
        return available(location).stream().filter(widget -> enabled(location, widget)).toList();
    }

    public boolean thirdColumn(TabLocation location) {
        JSONObject columns = root.optJSONObject("c");
        return columns == null || columns.optBoolean(location.getId(), true);
    }

    public int columns(TabLocation location) {
        return thirdColumn(location) ? TabLayout.MAX_COLUMNS : TabLayout.MAX_COLUMNS - 1;
    }

    public void setThirdColumn(TabLocation location, boolean value) {
        JSONObject columns = root.optJSONObject("c");
        if (columns == null) {
            columns = new JSONObject();
            root.put("c", columns);
        }
        columns.put(location.getId(), value);
        persist();
    }

    public void resetLocation(TabLocation location) {
        JSONObject widgets = root.optJSONObject(WIDGETS);
        if (widgets != null) widgets.remove(location.getId());
        JSONObject columns = root.optJSONObject("c");
        if (columns != null) columns.remove(location.getId());
        persist();
    }

    public void applyToAllLocations(TabLocation source, TabWidget widget) {
        JSONObject entry = entry(source, widget);
        for (TabLocation target : TabLocation.values()) {
            if (target == source || !widget.availableAt(target)) continue;
            JSONObject copy = new JSONObject(entry.toString());
            copy.remove("p");
            JSONObject existing = mutableEntry(target, widget);
            int priority = existing.optInt("p", Integer.MIN_VALUE);
            for (String key : existing.keySet().toArray(new String[0])) existing.remove(key);
            for (String key : copy.keySet()) existing.put(key, copy.get(key));
            if (priority != Integer.MIN_VALUE) existing.put("p", priority);
        }
        persist();
    }

    public void move(TabLocation location, TabWidget widget, int delta) {
        List<TabWidget> ordered = available(location).stream().filter(w -> w.unavailableReason() == null).toList();
        int index = ordered.indexOf(widget);
        int target = index + delta;
        if (index < 0 || target < 0 || target >= ordered.size()) return;
        List<TabWidget> reordered = new java.util.ArrayList<>(ordered);
        reordered.remove(index);
        reordered.add(target, widget);
        for (int i = 0; i < reordered.size(); i++) {
            mutableEntry(location, reordered.get(i)).put("p", i * 10);
        }
        persist();
    }

    private JSONObject entry(TabLocation location, TabWidget widget) {
        JSONObject widgets = root.optJSONObject(WIDGETS);
        if (widgets == null) return new JSONObject();
        JSONObject locationEntry = widgets.optJSONObject(location.getId());
        if (locationEntry == null) return new JSONObject();
        JSONObject entry = locationEntry.optJSONObject(widget.id());
        return entry == null ? new JSONObject() : entry;
    }

    private JSONObject mutableEntry(TabLocation location, TabWidget widget) {
        JSONObject widgets = root.optJSONObject(WIDGETS);
        if (widgets == null) {
            widgets = new JSONObject();
            root.put(WIDGETS, widgets);
        }
        JSONObject locationEntry = widgets.optJSONObject(location.getId());
        if (locationEntry == null) {
            locationEntry = new JSONObject();
            widgets.put(location.getId(), locationEntry);
        }
        JSONObject entry = locationEntry.optJSONObject(widget.id());
        if (entry == null) {
            entry = new JSONObject();
            locationEntry.put(widget.id(), entry);
        }
        return entry;
    }

    private void write(TabLocation location, TabWidget widget, String key, Object value) {
        mutableEntry(location, widget).put(key, value);
        persist();
    }

    private void persist() {
        sink.accept(root.toString());
    }
}
