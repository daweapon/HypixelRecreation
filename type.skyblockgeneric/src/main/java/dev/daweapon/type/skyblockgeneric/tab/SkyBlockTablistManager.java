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
        TabWidgetContext context = new TabWidgetContext(location, settings);
        List<TabLayout.Block> blocks = new ArrayList<>();
        for (TabWidget widget : settings.active(location)) {
            try {
                List<Text> lines = widget.lines(skyBlockPlayer, context);
                if (lines.isEmpty()) continue;
                blocks.add(new TabLayout.Block(lines,
                        settings.spacing(location, widget), settings.wrapping(location, widget)));
            } catch (Exception exception) {
                reportOnce(widget.getClass().getName(), exception);
            }
        }

        Text infoHeader = TablistModule.getCentered(Text.key("tablist.module.info"), player.getLocale());
        for (Text slot : TabLayout.layout(infoHeader, blocks)) {
            entries.add(slot == null
                    ? TablistModule.getGrayEntry()
                    : new TablistModule.TablistEntry(slot, TablistSkinRegistry.GRAY));
        }
        return entries;
    }
}
