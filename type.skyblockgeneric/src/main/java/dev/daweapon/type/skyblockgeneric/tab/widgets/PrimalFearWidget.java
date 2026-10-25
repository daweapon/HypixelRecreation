package dev.daweapon.type.skyblockgeneric.tab.widgets;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.greatspook.GreatSpookEvent;
import dev.daweapon.type.skyblockgeneric.greatspook.PrimalFearProgress;
import dev.daweapon.type.skyblockgeneric.tab.TabWidget;
import dev.daweapon.type.skyblockgeneric.tab.TabWidgetContext;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.Material;

import java.util.List;

public final class PrimalFearWidget implements TabWidget {
    @Override
    public String id() {
        return "primal_fear";
    }

    @Override
    public String displayName() {
        return "Primal Fear";
    }

    @Override
    public Material icon() {
        return Material.CAULDRON;
    }

    @Override
    public int defaultPriority() {
        return 45;
    }

    @Override
    public String description() {
        return "Shows how long until your next Primal Fear can spawn during the Great Spook.";
    }

    @Override
    public List<Text> lines(SkyBlockPlayer player, TabWidgetContext context) {
        if (!GreatSpookEvent.isActive() || player.getSkyblockDataHandler() == null) return List.of();
        long remaining = PrimalFearProgress.remainingMillis(player);
        return List.of(
                Text.of("<5><l>Great Spook:"),
                remaining <= 0 ? Text.of(" <7>Next Fear: <a>Ready") : Text.of(" <7>Next Fear: <e>{}", TabTime.format(remaining)));
    }
}
