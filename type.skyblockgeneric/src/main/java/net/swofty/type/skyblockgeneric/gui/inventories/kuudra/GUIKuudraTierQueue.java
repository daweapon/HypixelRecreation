package net.swofty.type.skyblockgeneric.gui.inventories.kuudra;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraAvailability;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraProgress;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * The step after picking a tier: queue with your party or browse the Kuudra Party Finder. Both stop
 * at the Kuudra hook until the fight exists. The layout is an estimate; the wiki has no UI page for it.
 */
public class GUIKuudraTierQueue extends StatelessView {
    private final KuudraTier tier;

    public GUIKuudraTierQueue(KuudraTier tier) {
        this.tier = tier;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(tier.getDisplayName() + " Tier", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 31);
        Components.back(layout, 30, ctx);

        layout.slot(13, (_, c) -> GUICragSelectTier.tierItem((SkyBlockPlayer) c.player(), tier, false));
        layout.slot(11, (_, c) -> ItemStacks.item(Material.LIME_TERRACOTTA, """
                <a>Queue
                <7>Queue up for the <c>%s Tier <7>with
                <7>your party, or alone.

                %s
                <e>Click to queue!""".formatted(tier.getDisplayName(), KuudraAvailability.UNAVAILABLE_LORE)),
                (_, c) -> queue((SkyBlockPlayer) c.player()));
        layout.slot(15, (_, c) -> ItemStacks.item(Material.WRITABLE_BOOK, """
                <a>Party Finder
                <7>Find other adventurers to fight
                <c>Kuudra <7>with, or list your own party.

                %s
                <e>Click to browse!""".formatted(KuudraAvailability.UNAVAILABLE_LORE)),
                (_, c) -> {
                    KuudraAvailability.blockIfUnavailable((SkyBlockPlayer) c.player());
                    c.player().closeInventory();
                });
    }

    private void queue(SkyBlockPlayer player) {
        player.closeInventory();
        if (KuudraAvailability.blockIfUnavailable(player)) return;
        if (!KuudraProgress.meetsRequirements(player, tier)) {
            player.sendMessage("<c>You don't meet the requirements for this tier!");
        }
    }
}
