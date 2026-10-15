package dev.daweapon.type.hub.gui.carnival;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.carnival.Carnivals;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// Doug's Carnival Masks stock, from the wiki's Doug/UI subpage.
public class GUICarnivalMasks extends StatelessView {
    private record Offer(ItemType item, int cost, @Nullable String unavailableReason) {}

    private static final List<Offer> OFFERS = List.of(
            new Offer(ItemType.CARNIVAL_MASK_BAG, 250, "The Carnival Mask Bag's storage isn't in this server yet."),
            new Offer(ItemType.ZOMBIE_MASK, 500, null),
            new Offer(ItemType.SALMON_MASK, 500, null),
            new Offer(ItemType.ARMADILLO_MASK, 1_000, null),
            new Offer(ItemType.PARROT_MASK, 1_000, null),
            new Offer(ItemType.BEE_MASK, 2_000, null),
            new Offer(ItemType.FROG_MASK, 2_000, null));

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Carnival Masks", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);
        for (int i = 0; i < OFFERS.size(); i++) {
            Offer offer = OFFERS.get(i);
            layout.slot(10 + i, (_, c) -> offerItem((SkyBlockPlayer) c.player(), offer), (_, c) -> {
                buy((SkyBlockPlayer) c.player(), offer);
                c.session(DefaultState.class).refresh();
            });
        }
        layout.slot(50, (_, c) -> CarnivalTokensDisplay.of((SkyBlockPlayer) c.player()));
    }

    private static ItemStack.Builder offerItem(SkyBlockPlayer player, Offer offer) {
        ItemStack.Builder builder = PlayerItemUpdater.playerUpdate(player, new SkyBlockItem(offer.item()).getItemStack());
        String cost = "\n<7>Cost\n<e>" + StringUtility.commaify(offer.cost()) + " Carnival Tokens\n\n";
        if (offer.unavailableReason() != null) {
            return ItemStacks.appendLore(builder, cost + "<8>" + offer.unavailableReason() + "\n<c>Unavailable");
        }
        boolean affordable = Carnivals.profile(player).getTokens() >= offer.cost();
        return ItemStacks.appendLore(builder, cost + (affordable ? "<e>Click to trade!" : "<c>You don't have enough Carnival Tokens!"));
    }

    private static void buy(SkyBlockPlayer player, Offer offer) {
        if (offer.unavailableReason() != null) {
            player.sendMessage("<c>" + offer.unavailableReason());
            return;
        }
        if (!Carnivals.spendTokens(player, offer.cost())) {
            player.sendMessage("<c>You don't have enough Carnival Tokens!");
            return;
        }
        SkyBlockItem item = new SkyBlockItem(offer.item());
        player.addAndUpdateItem(item);
        player.sendMessage("<a>You bought " + item.getDisplayName() + "<a>!");
    }
}
