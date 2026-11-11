package dev.daweapon.type.hub.gui.carnival;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.inventory.InventoryType;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.carnival.Carnival;
import dev.daweapon.type.skyblockgeneric.carnival.Carnivals;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

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
        layout.slot(22, (_, c) -> ItemStacks.name(ItemStacks.appendLore(
                PlayerItemUpdater.playerUpdate((SkyBlockPlayer) c.player(),
                        new SkyBlockItem(ItemType.CARNIVAL_TICKET, Carnival.DAILY_TICKETS).getItemStack()),
                "\n<e>Click to claim!"), "<a>Carnival Ticket<8> x" + Carnival.DAILY_TICKETS), (_, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            c.player().closeInventory();
            if (!Carnivals.canClaimDailyTickets(Carnivals.profile(player))) return;
            Carnivals.claimDailyTickets(player);
            player.sendMessage("<a>You claimed <a>Carnival Ticket <8>x" + Carnival.DAILY_TICKETS + "<a>!");
            player.playSound(Sound.sound(Key.key("block.note_block.pling"), Sound.Source.PLAYER, 1f, 1f));
            onClaim.accept(player);
        });
    }

    @Override
    public void onOpen(DefaultState state, ViewContext ctx) {
        ctx.player().playSound(Sound.sound(Key.key("block.lever.click"), Sound.Source.PLAYER, 1f, 1f));
    }
}
