package dev.daweapon.type.skyblockgeneric.tab.widgets;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.tab.TabWidget;
import dev.daweapon.type.skyblockgeneric.tab.TabWidgetContext;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.Material;

import java.util.List;

public final class FireSalesWidget implements TabWidget {
    @Override
    public String id() {
        return "fire_sales";
    }

    @Override
    public String displayName() {
        return "Fire Sales";
    }

    @Override
    public Material icon() {
        return Material.GOLD_INGOT;
    }

    @Override
    public int defaultPriority() {
        return 40;
    }

    @Override
    public String description() {
        return "Shows the Fire Sales that are active or coming up.";
    }

    @Override
    public String unavailableReason() {
        return "Fire Sales are not built yet (Roadmap #51).";
    }

    @Override
    public List<Text> lines(SkyBlockPlayer player, TabWidgetContext context) {
        return List.of();
    }
}
