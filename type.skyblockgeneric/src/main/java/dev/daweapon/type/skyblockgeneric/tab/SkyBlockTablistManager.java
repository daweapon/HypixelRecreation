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
    private final TabPlayerBlock playerBlock;
    private final SkyBlockPlayersOnlineModule plainModule;

    public SkyBlockTablistManager(TabLocation location) {
        this(location, (Function<SkyBlockPlayer, String>) ignored -> "");
    }

    public SkyBlockTablistManager(TabLocation location, Function<SkyBlockPlayer, String> nameSuffix) {
        this.location = location;
        this.plainModule = new SkyBlockPlayersOnlineModule(1, nameSuffix);
        this.playerBlock = viewer -> TabPlayers.names(viewer, nameSuffix);
    }

    public SkyBlockTablistManager(TabLocation location, TabPlayerBlock playerBlock) {
        this.location = location;
        this.plainModule = new SkyBlockPlayersOnlineModule(1);
        this.playerBlock = playerBlock;
    }

    @Override
    public List<TablistModule> getModules() {
        return List.of(plainModule);
    }

    @Override
    protected List<TablistModule.TablistEntry> buildEntries(HypixelPlayer player) {
        SkyBlockPlayer skyBlockPlayer = (SkyBlockPlayer) player;
        TabSettings settings = TabSettings.of(skyBlockPlayer);

        if (!settings.playerListInfo()) return plainModule.plainEntries(skyBlockPlayer);

        List<TabLine> flow = layoutFor(skyBlockPlayer, location, settings, playerBlock, reportingFailures());
        List<TablistModule.TablistEntry> entries = new ArrayList<>();
        for (int column = 0; column < TabLayout.MAX_COLUMNS; column++) {
            for (int row = 0; row < TabLayout.ROWS; row++) {
                int index = column * TabLayout.ROWS + row;
                TabLine line = index < flow.size() ? flow.get(index) : null;
                entries.add(line == null
                        ? TablistModule.getGrayEntry()
                        : new TablistModule.TablistEntry(line.text(), line.skin()));
            }
        }
        return entries;
    }

    private java.util.function.BiConsumer<TabWidget, Exception> reportingFailures() {
        return (widget, exception) -> reportOnce(widget.getClass().getName(), exception);
    }

    public static List<TabLine> layoutFor(SkyBlockPlayer player, TabLocation location, TabSettings settings,
                                          TabPlayerBlock playerBlock,
                                          java.util.function.BiConsumer<TabWidget, Exception> onFailure) {
        TabWidgetContext context = new TabWidgetContext(location, settings);
        List<TabLayout.Block> info = new ArrayList<>();
        List<TabLayout.Block> other = new ArrayList<>();
        for (TabWidget widget : settings.active(location)) {
            try {
                List<Text> lines = widget.lines(player, context);
                if (lines.isEmpty()) continue;
                (widget.otherSection() ? other : info).add(new TabLayout.Block(
                        lines.stream().map(line -> line == null ? null : TabLine.of(line)).toList(),
                        settings.spacing(location, widget), settings.wrapping(location, widget)));
            } catch (Exception exception) {
                onFailure.accept(widget, exception);
            }
        }

        List<TabLayout.Block> blocks = new ArrayList<>();
        if (playerBlock != null) {
            List<TabLine> players = new ArrayList<>(playerBlock.lines(player));
            players.add(null);
            blocks.add(new TabLayout.Block(players));
        }
        blocks.add(new TabLayout.Block(List.of(sectionHeader("tablist.module.info", player))));
        blocks.addAll(info);
        if (!other.isEmpty()) {
            blocks.add(new TabLayout.Block(List.of(sectionHeader("tablist.module.other", player))));
            blocks.addAll(other);
        }
        return TabLayout.layout(blocks, settings.columns(location));
    }

    private static TabLine sectionHeader(String key, SkyBlockPlayer player) {
        return TabLine.header(TablistModule.getCentered(Text.key(key), player.getLocale()), TablistSkinRegistry.CYAN);
    }

    public static List<Text> previewLines(SkyBlockPlayer player, TabLocation location, TabSettings settings) {
        List<Text> lines = new ArrayList<>();
        for (TabLine slot : layoutFor(player, location, settings, null, (widget, exception) -> { })) {
            lines.add(slot == null ? Text.empty() : slot.text());
        }
        while (!lines.isEmpty() && lines.getLast().plain(java.util.Locale.ROOT).isEmpty()) lines.removeLast();
        return lines;
    }
}
