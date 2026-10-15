package dev.daweapon.type.skyblockgeneric.gui.inventories.accessory;

import net.minestom.server.inventory.InventoryType;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.accessory.enrichment.Enrichment;
import dev.daweapon.type.skyblockgeneric.accessory.enrichment.Enrichments;
import dev.daweapon.type.skyblockgeneric.accessory.power.StatText;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/** The Accessory Enrichment Swapper's menu: pick the enrichment every enriched accessory in the bag becomes. */
public class GUIEnrichmentSwapper extends StatelessView {
    private static final int[] SLOTS = {10, 11, 12, 13, 14, 15, 16, 20, 21, 22, 23, 24};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Accessory Enrichment Swapper", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 31);
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        int enriched = Enrichments.enrichedInBag(player.getAccessoryBag());

        Enrichment[] enrichments = Enrichment.values();
        for (int i = 0; i < enrichments.length; i++) {
            Enrichment enrichment = enrichments[i];
            ItemType type = enrichment.item();
            if (type == null) continue;
            layout.slot(SLOTS[i], (s, c) -> ItemStacks.appendLore(
                            PlayerItemUpdater.playerUpdate(player, new SkyBlockItem(type).getItemStack()),
                            "\n<7>Swaps <e>" + enriched + " <7>enriched accessories to\n"
                                    + StatText.line(enrichment.getStatistic(), enrichment.getAmount()) + "<7>.\n\n<e>Click to swap!"),
                    (click, c) -> {
                        if (Enrichments.useSwapper(player, enrichment)) c.player().closeInventory();
                    });
        }
    }
}
