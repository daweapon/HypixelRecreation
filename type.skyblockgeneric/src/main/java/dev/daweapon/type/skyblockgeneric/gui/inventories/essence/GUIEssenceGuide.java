package dev.daweapon.type.skyblockgeneric.gui.inventories.essence;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
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
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// The Essence Guide lists every essence and how much of it the profile holds. Its per-essence item
// lists are left out: essence upgrading (✪) isn't in this server yet.
public class GUIEssenceGuide extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Essence Guide", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);
        for (EssenceType type : EssenceType.values()) {
            EssenceGuideEntry entry = EssenceGuideEntry.of(type);
            layout.slot(entry.slot(), (_, c) -> icon(entry, Essences.get((SkyBlockPlayer) c.player(), type)));
        }
    }

    static ItemStack.Builder icon(EssenceGuideEntry entry, long amount) {
        EssenceType type = entry.type();
        StringBuilder lore = new StringBuilder(type.isAvailable() ? "<a>" : "<8>").append(type.getFullName()).append("\n")
                .append("<7>View a list of items that can be\n<7>converted and upgraded using\n")
                .append(entry.colour()).append(type.getFullName()).append("<7>.\n\n");
        if (entry.flavour() != null) lore.append("<7><o>").append(entry.flavour()).append("\n\n");
        lore.append("<7>Your ").append(type.getFullName()).append(": <d>").append(StringUtility.commaify(amount));
        if (!type.isAvailable()) lore.append("\n\n<8>").append(type.getUnavailableReason());
        return ItemStacks.head(entry.texture(), lore.toString());
    }
}
