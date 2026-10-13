package net.swofty.type.dwarvenmines.gui;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.ServerType;
import net.swofty.commons.StringUtility;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.crystalhollows.CrystalHollowsAccess;
import net.swofty.type.skyblockgeneric.hotm.HotmService;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class GUIGwendolyn extends StatelessView {
    private static final String SPEAKER = "<e>[NPC] <b>Gwendolyn<f>: ";

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Crystal Hollows Pass", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        layout.slot(11, (_, _) -> passItem(player), (_, c) -> buy(player, c));
        layout.slot(15, (_, _) -> minecartItem(player), (_, _) -> ride(player));
        Components.close(layout, 31);
    }

    private ItemStack.Builder passItem(SkyBlockPlayer player) {
        if (CrystalHollowsAccess.hasPermanentAccess(player)) {
            return ItemStacks.item(Material.NETHER_STAR, "<a>Crystal Hollows Pass\n<7>You have <a>permanent access<7> to the\n<7>Crystal Hollows.");
        }
        String left = CrystalHollowsAccess.hasValidPass(player)
                ? "\n<7>Time left: <a>" + HotmService.formatDuration(CrystalHollowsAccess.millisLeft(player)) + "\n"
                : "\n";
        return ItemStacks.item(Material.PAPER, "<a>Crystal Hollows Pass\n<7>Grants access to the Crystal Hollows\n<7>for <e>6 hours<7>.\n" + left
                + "\n<7>Cost: <6>" + StringUtility.commaify((int) CrystalHollowsAccess.PASS_PRICE) + " coins\n\n<e>Click to purchase!");
    }

    private ItemStack.Builder minecartItem(SkyBlockPlayer player) {
        if (!CrystalHollowsAccess.hasValidPass(player)) {
            return ItemStacks.item(Material.MINECART, "<c>Minecart\n<7>You need a valid pass to ride\n<7>to the Crystal Hollows.");
        }
        return ItemStacks.item(Material.MINECART, "<a>Minecart\n<7>Ride to the <5>Crystal Hollows<7>.\n\n<e>Click to travel!");
    }

    private void buy(SkyBlockPlayer player, ViewContext click) {
        if (CrystalHollowsAccess.hasValidPass(player)) {
            player.sendMessage(SPEAKER + "You already have access to the Crystal Hollows!");
            return;
        }
        String problem = CrystalHollowsAccess.buyPass(player);
        if (problem != null) {
            player.sendMessage("<c>" + problem);
            return;
        }
        player.sendMessage(SPEAKER + "Great! Now hop on into the Minecart and I'll get you on your way!");
        click.session(DefaultState.class).refresh();
    }

    private void ride(SkyBlockPlayer player) {
        if (!CrystalHollowsAccess.hasValidPass(player)) {
            player.sendMessage("<c>You need a valid Crystal Hollows pass to ride the Minecart!");
            return;
        }
        player.closeInventory();
        player.sendMessage(SPEAKER + "Good luck in the mines!");
        player.sendTo(ServerType.SKYBLOCK_CRYSTAL_HOLLOWS);
    }
}
