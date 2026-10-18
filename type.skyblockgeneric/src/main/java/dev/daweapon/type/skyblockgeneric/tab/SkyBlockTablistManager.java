package dev.daweapon.type.skyblockgeneric.tab;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.tab.TablistModule;
import dev.daweapon.type.generic.tab.TablistManager;
import dev.daweapon.type.generic.tab.TablistSkinRegistry;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.tabmodules.SkyBlockPlayersOnlineModule;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class SkyBlockTablistManager extends TablistManager {
    private final TabLocation location;
    private final TablistModule playersModule;
    private final SkyBlockPlayersOnlineModule plainModule;

    public SkyBlockTablistManager(TabLocation location) {
        this(location, ignored -> "");
    }

    public SkyBlockTablistManager(TabLocation location, Function<SkyBlockPlayer, String> nameSuffix) {
        this.location = location;
        this.plainModule = new SkyBlockPlayersOnlineModule(1, nameSuffix);
        this.playersModule = plainModule;
    }

    public SkyBlockTablistManager(TabLocation location, TablistModule playerColumn) {
        this.location = location;
        this.plainModule = new SkyBlockPlayersOnlineModule(1);
        this.playersModule = playerColumn;
    }

    @Override
    public List<TablistModule> getModules() {
        return List.of(playersModule);
    }

    @Override
    protected List<TablistModule.TablistEntry> buildEntries(HypixelPlayer player) {
        SkyBlockPlayer skyBlockPlayer = (SkyBlockPlayer) player;
        TabSettings settings = TabSettings.of(skyBlockPlayer);

        if (!settings.playerListInfo()) return plainModule.plainEntries(skyBlockPlayer);

        List<TablistModule.TablistEntry> entries = new ArrayList<>(playersModule.getEntries(skyBlockPlayer));
        List<Text> widgetSlots = layoutFor(skyBlockPlayer, location, settings, reportingFailures());
        List<Integer> enabled = settings.enabledColumns(location);
        for (int column = 0; column < TabLayout.WIDGET_COLUMNS; column++) {
            int position = enabled.indexOf(column);
            for (int row = 0; row < TabLayout.ROWS; row++) {
                Text slot = position < 0 ? null : widgetSlots.get(position * TabLayout.ROWS + row);
                entries.add(slot == null
                        ? TablistModule.getGrayEntry()
                        : new TablistModule.TablistEntry(slot, TablistSkinRegistry.GRAY));
            }
        }
        return entries;
    }

    private java.util.function.BiConsumer<TabWidget, Exception> reportingFailures() {
        return (widget, exception) -> reportOnce(widget.getClass().getName(), exception);
    }

    public static List<Text> layoutFor(SkyBlockPlayer player, TabLocation location, TabSettings settings,
                                       java.util.function.BiConsumer<TabWidget, Exception> onFailure) {
        TabWidgetContext context = new TabWidgetContext(location, settings);
        List<TabLayout.Block> info = new ArrayList<>();
        List<TabLayout.Block> other = new ArrayList<>();
        for (TabWidget widget : settings.active(location)) {
            try {
                List<Text> lines = widget.lines(player, context);
                if (lines.isEmpty()) continue;
                (widget.otherSection() ? other : info).add(new TabLayout.Block(lines,
                        settings.spacing(location, widget), settings.wrapping(location, widget)));
            } catch (Exception exception) {
                onFailure.accept(widget, exception);
            }
        }
        List<TabLayout.Block> blocks = new ArrayList<>(info);
        if (!other.isEmpty()) {
            blocks.add(new TabLayout.Block(List.of(
                    TablistModule.getCentered(Text.key("tablist.module.other"), player.getLocale())), false, false));
            blocks.addAll(other);
        }
        Text infoHeader = TablistModule.getCentered(Text.key("tablist.module.info"), player.getLocale());
        return TabLayout.layout(infoHeader, blocks, settings.enabledColumns(location).size());
    }

    public static List<Text> previewLines(SkyBlockPlayer player, TabLocation location, TabSettings settings) {
        List<Text> lines = new ArrayList<>();
        for (Text slot : layoutFor(player, location, settings, (widget, exception) -> { })) {
            if (slot != null) lines.add(slot);
        }
        return lines;
    }
}
