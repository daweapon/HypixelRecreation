package dev.daweapon.type.skyblockgeneric.tab.widgets;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.crystalhollows.CrystalNucleus;
import dev.daweapon.type.skyblockgeneric.crystalhollows.NucleusCrystal;
import dev.daweapon.type.skyblockgeneric.tab.TabLocation;
import dev.daweapon.type.skyblockgeneric.tab.TabWidgetContext;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.List;

final class CrystalsWidget extends AreaWidget {
    CrystalsWidget() {
        super("crystals", "Crystals", Material.AMETHYST_SHARD, 80, TabLocation.CRYSTAL_HOLLOWS);
    }

    @Override
    public List<Text> lines(SkyBlockPlayer player, TabWidgetContext context) {
        List<Text> lines = new ArrayList<>();
        lines.add(Text.of("<b><l>Crystals:"));
        for (NucleusCrystal crystal : NucleusCrystal.values()) {
            lines.add(Text.of(" " + crystal.getColour() + crystal.getDisplayName() + "<f>: "
                    + CrystalNucleus.state(player, crystal).label()));
        }
        return lines;
    }
}
