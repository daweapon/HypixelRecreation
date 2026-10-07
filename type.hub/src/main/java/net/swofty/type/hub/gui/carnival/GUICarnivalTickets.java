package net.swofty.type.hub.gui.carnival;

import net.minestom.server.inventory.InventoryType;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.carnival.Carnival;
import net.swofty.type.skyblockgeneric.carnival.Carnivals;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.function.Consumer;

// The Carnival Leader's daily ticket menu, from the wiki's Carnival Leader/UI subpage.
public class GUICarnivalTickets extends StatelessView {
    private final Consumer<SkyBlockPlayer> onClaim;

    public GUICarnivalTickets(Consumer<SkyBlockPlayer> onClaim) {
        this.onClaim = onClaim;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Claim Reward", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        layout.slot(22, (_, c) -> ItemStacks.appendLore(
                PlayerItemUpdater.playerUpdate((SkyBlockPlayer) c.player(),
                        new SkyBlockItem(ItemType.CARNIVAL_TICKET, Carnival.DAILY_TICKETS).getItemStack()),
                "\n<e>Click to claim!"), (_, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            c.player().closeInventory();
            if (!Carnivals.canClaimDailyTickets(Carnivals.profile(player))) return;
            Carnivals.claimDailyTickets(player);
            onClaim.accept(player);
        });
    }
}
