package dev.daweapon.type.dwarvenmines.gui;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.crystalhollows.CrystalHollowsAccess;
import dev.daweapon.type.skyblockgeneric.hotm.HotmService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;

public class GUIGwendolyn extends StatelessView {
    private static final String SPEAKER = "<e>[NPC] <5>Gwendolyn<f>: ";

    private static final Map<Integer, Material> PANES = Map.of(
            12, Material.LIME_STAINED_GLASS_PANE, 13, Material.GREEN_STAINED_GLASS_PANE, 14, Material.BLACK_STAINED_GLASS_PANE,
            21, Material.GRAY_STAINED_GLASS_PANE, 23, Material.BLACK_STAINED_GLASS_PANE,
            30, Material.BLACK_STAINED_GLASS_PANE, 31, Material.BLACK_STAINED_GLASS_PANE, 32, Material.BLACK_STAINED_GLASS_PANE);

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Crystal Hollows", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        PANES.forEach((slot, material) -> layout.slot(slot, (_, _) -> Components.asFiller(material)));
        layout.slot(22, (_, _) -> minecartItem(player), (_, c) -> enter(player, c));
        layout.slot(50, (_, _) -> infoItem());
        Components.close(layout, 49);
    }

    private ItemStack.Builder infoItem() {
        return ItemStacks.item(Material.REDSTONE_TORCH, "<3>Crystal Hollows Info\n"
                + "<7>The <3>Crystal Hollows <7>is a\n<7>huge random underground area\n<7>that hosts many different\n"
                + "<6>ores<7>, <d>gems<7>, <a>biomes<7>,\n<7>and <c>creatures<7>.\n\n"
                + "<d>Gemstones <7>are brightly\n<7>colored shards embedded in the\n<7>walls of most <8>tunnels<7>. You\n"
                + "<7>will find different types of\n<d>Gemstones <7>in different\n<a>biomes <7>except for <c>Ruby\n"
                + "<7>that appears to be found\n<7>everywhere.\n\n"
                + "<7>On your <a>adventures <7>you\n<7>will find many structures. Some\n<7>abandoned, some not. Structures\n"
                + "<7>such as these can contain <5>epic\n<5>loot <7>or <c>dangerous foes <7>so\n<7>always be on the look out.\n\n<e>Good luck!");
    }

    private ItemStack.Builder minecartItem(SkyBlockPlayer player) {
        if (CrystalHollowsAccess.hasPermanentAccess(player)) {
            return ItemStacks.item(Material.MINECART, "<3>Join the Crystal Hollows\n\n<7>You have <a>permanent access <7>to the\n<2>Crystal Hollows<7>.\n\n<e>Click to travel!");
        }
        if (CrystalHollowsAccess.hasValidPass(player)) {
            return ItemStacks.item(Material.MINECART, "<3>Join the Crystal Hollows\n\n<7>Your pass is active for another\n<a>"
                    + HotmService.formatDuration(CrystalHollowsAccess.millisLeft(player)) + "<7>.\n\n<e>Click to travel!");
        }
        return ItemStacks.item(Material.MINECART, "<3>Join the Crystal Hollows\n\n<7>This pass grants you access to the\n<2>Crystal Hollows<7> for <6>6 hours<7>.\n\n"
                + "<7>As long as the pass is active, you can\n<7>travel back and forth using the <b>Minecart\n<7>in your <a>SkyBlock Menu<7>.\n\n"
                + "<7>Cost\n<6>" + StringUtility.commaify((int) CrystalHollowsAccess.PASS_PRICE) + " Coins\n\n<e>Click to purchase!");
    }

    private void enter(SkyBlockPlayer player, ViewContext click) {
        if (CrystalHollowsAccess.hasValidPass(player)) {
            ride(player);
            return;
        }
        buy(player, click);
    }

    private void buy(SkyBlockPlayer player, ViewContext click) {
        String problem = CrystalHollowsAccess.buyPass(player);
        if (problem != null) {
            player.sendMessage("<c>" + problem);
            return;
        }
        player.sendMessage(SPEAKER + "Great! Now hop on into the Minecart and I'll get you on your way!");
        click.session(DefaultState.class).refresh();
    }

    private void ride(SkyBlockPlayer player) {
        player.closeInventory();
        player.sendMessage(SPEAKER + "Good luck in the mines!");
        player.sendTo(ServerType.SKYBLOCK_CRYSTAL_HOLLOWS);
    }
}
