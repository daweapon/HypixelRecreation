package dev.daweapon.type.skyblockgeneric.tab.widgets;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.garden.composter.ComposterService;
import dev.daweapon.type.skyblockgeneric.garden.composter.ComposterState;
import dev.daweapon.type.skyblockgeneric.tab.TabLocation;
import dev.daweapon.type.skyblockgeneric.tab.TabWidgetContext;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.Material;

import java.util.List;
import java.util.Locale;

final class ComposterWidget extends AreaWidget {
    ComposterWidget() {
        super("composter", "Composter", Material.COMPOSTER, 80, TabLocation.GARDEN);
    }

    @Override
    public List<Text> lines(SkyBlockPlayer player, TabWidgetContext context) {
        ComposterState state = ComposterService.state(player);
        long seconds = state.running() ? (long) Math.ceil(state.getRemainingSeconds()) : 0;
        return List.of(
                Text.of("<a><l>Composter:"),
                Text.of(" <7>Organic Matter: <a>{}", StringUtility.commaify((long) state.getOrganicMatter())),
                Text.of(" <7>Fuel: <a>{}", StringUtility.commaify((long) state.getFuel())),
                Text.of(" <7>Time Left: <a>{}", String.format(Locale.ROOT, "%02d:%02d:%02d",
                        seconds / 3600, seconds % 3600 / 60, seconds % 60)),
                Text.of(" <7>Stored Compost: <a>{}", StringUtility.commaify(state.getStoredCompost())));
    }
}
