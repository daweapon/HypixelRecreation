package dev.daweapon.type.skyblockgeneric.tab.widgets;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.glacite.mineshaft.MineshaftPity;
import dev.daweapon.type.skyblockgeneric.glacite.mineshaft.MineshaftService;
import dev.daweapon.type.skyblockgeneric.tab.TabLocation;
import dev.daweapon.type.skyblockgeneric.tab.TabWidget;
import dev.daweapon.type.skyblockgeneric.tab.TabWidgetContext;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.Material;

import java.util.List;
import java.util.Set;

public final class MineshaftPityWidget implements TabWidget {

    @Override
    public String id() {
        return "mineshaft_pity";
    }

    @Override
    public String displayName() {
        return "Mineshaft Pity";
    }

    @Override
    public Material icon() {
        return Material.IRON_PICKAXE;
    }

    @Override
    public int defaultPriority() {
        return 112;
    }

    @Override
    public Set<TabLocation> locations() {
        return Set.of(TabLocation.DWARVEN_MINES);
    }

    @Override
    public List<Text> lines(SkyBlockPlayer player, TabWidgetContext context) {
        return List.of(Text.of("<b><l>Mineshaft:"),
                Text.of(" <b>Pity: <f>{}<7>/{}", MineshaftPity.displayed(MineshaftService.pityCounter(player)), MineshaftPity.START));
    }
}
