package dev.daweapon.type.skyblockgeneric.tab.widgets;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.hotm.HotmPowder;
import dev.daweapon.type.skyblockgeneric.hotm.HotmService;
import dev.daweapon.type.skyblockgeneric.tab.TabLocation;
import dev.daweapon.type.skyblockgeneric.tab.TabWidget;
import dev.daweapon.type.skyblockgeneric.tab.TabWidgetContext;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class PowderWidget implements TabWidget {

    @Override
    public String id() {
        return "powder";
    }

    @Override
    public String displayName() {
        return "Powders";
    }

    @Override
    public Material icon() {
        return Material.GREEN_DYE;
    }

    @Override
    public int defaultPriority() {
        return 110;
    }

    @Override
    public Set<TabLocation> locations() {
        return Set.of(TabLocation.DWARVEN_MINES, TabLocation.CRYSTAL_HOLLOWS);
    }

    @Override
    public List<Text> lines(SkyBlockPlayer player, TabWidgetContext context) {
        List<Text> lines = new ArrayList<>();
        lines.add(Text.of("<2><l>Powders:"));
        var data = HotmService.data(player);
        for (HotmPowder powder : HotmPowder.values()) {
            String name = powder.getDisplayName().replace(" Powder", "");
            lines.add(Text.of(" " + powder.getColour() + "{}: <f>{:,}", name, data.powder(powder)));
        }
        return lines;
    }
}
