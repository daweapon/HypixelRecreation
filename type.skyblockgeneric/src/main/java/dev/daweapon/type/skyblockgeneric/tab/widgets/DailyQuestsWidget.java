package dev.daweapon.type.skyblockgeneric.tab.widgets;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.tab.TabWidget;
import dev.daweapon.type.skyblockgeneric.tab.TabWidgetContext;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.Material;

import java.util.List;

public final class DailyQuestsWidget implements TabWidget {
    @Override
    public String id() {
        return "daily_quests";
    }

    @Override
    public String displayName() {
        return "Daily Quests";
    }

    @Override
    public Material icon() {
        return Material.WRITABLE_BOOK;
    }

    @Override
    public int defaultPriority() {
        return 25;
    }

    @Override
    public String description() {
        return "Shows your daily quests and when they reset.";
    }

    @Override
    public String unavailableReason() {
        return "There are no daily quests yet (Irania, Matriarch, Fetcher and Puzzler are not built).";
    }

    @Override
    public List<Text> lines(SkyBlockPlayer player, TabWidgetContext context) {
        return List.of();
    }
}
