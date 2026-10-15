package dev.daweapon.type.skyblockgeneric.gui.inventories.essence;

import net.minestom.server.inventory.InventoryType;
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

public class GUIDungeonEssenceShops extends StatelessView {
    private static final EssenceType[] SHOPS = {EssenceType.UNDEAD, EssenceType.WITHER};
    private static final int[] SLOTS = {21, 23};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Essence Shop", InventoryType.CHEST_5_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 40);
        Components.back(layout, 39, ctx);
        for (int i = 0; i < SHOPS.length; i++) {
            EssenceType essence = SHOPS[i];
            EssenceGuideEntry entry = EssenceGuideEntry.of(essence);
            layout.slot(SLOTS[i], (_, c) -> ItemStacks.head(entry.texture(), """
                    <a>{} Essence Shop
                    <7>Exchange {}{} Essence <7>for perks!

                    <7>Your {} Essence: <d>{}

                    <e>Click to view!""", essence.getDisplayName(), entry.colour(), essence.getDisplayName(),
                    essence.getDisplayName(), StringUtility.commaify(Essences.get((SkyBlockPlayer) c.player(), essence))),
                    (_, c) -> c.push(new GUIEssenceShop(essence)));
        }
    }
}
