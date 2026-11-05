package dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.skills;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.essence.EssenceType;
import dev.daweapon.type.skyblockgeneric.essence.Essences;
import dev.daweapon.type.skyblockgeneric.gui.inventories.essence.EssenceGuideEntry;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;

import java.util.Map;

public class GUIYourEssence extends StatelessView {
    private static final Map<EssenceType, Integer> SLOTS = Map.of(
            EssenceType.WITHER, 11,
            EssenceType.SPIDER, 13,
            EssenceType.UNDEAD, 15,
            EssenceType.DRAGON, 28,
            EssenceType.GOLD, 30,
            EssenceType.DIAMOND, 32,
            EssenceType.ICE, 34);

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Your Essence", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);

        SLOTS.forEach((type, slot) -> {
            EssenceGuideEntry entry = EssenceGuideEntry.of(type);
            layout.slot(slot, (s, c) -> {
                long owned = Essences.get((SkyBlockPlayer) c.player(), type);
                StringBuilder lore = new StringBuilder("<a>").append(type.getFullName()).append("\n")
                        .append("<7>You currently own <a>").append(StringUtility.commaify(owned)).append("\n")
                        .append("<d>").append(type.getFullName()).append("<7>.");
                if (entry.flavour() != null) lore.append("\n\n<7><o>").append(wrapFlavour(entry.flavour()));
                return ItemStacks.head(entry.texture(), lore.toString());
            });
        });
    }

    private static String wrapFlavour(String flavour) {
        int split = flavour.indexOf(' ', Math.min(flavour.length(), 20));
        if (split < 0) return flavour;
        return flavour.substring(0, split) + "\n<7><o>" + flavour.substring(split + 1);
    }
}
