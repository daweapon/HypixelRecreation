package net.swofty.type.hub.gui.farmingcontest;

import net.minestom.server.inventory.InventoryType;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.data.datapoints.DatapointInteger;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.bags.GUIAccessoryBag;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.levels.SkyBlockXPSource;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

// Jacobus' Accessory Bag Upgrades menu, from the wiki's Jacobus/UI subpage and upgrade table.
public class GUIJacobus extends StatelessView {
    public static final int MAX_UPGRADES = 99;
    private static final int SLOTS_PER_UPGRADE = 2;
    private static final int REGISTER_AT_UPGRADES = 5;
    private static final int XP_PER_UPGRADE = 2;
    private static final String REGISTER_TEXTURE = "3b648b9a44e280bcdf25f4a66a97bd5c33542e5e82415e15b475c6b999b8d635";
    private static final String BAG_TEXTURE = "396ce13ff6155fdf3235d8d22174c5de4bf5512f1adeda1afa3fc28180f3f7";

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Accessory Bag Upgrades", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 31);

        layout.slot(11, (_, c) -> ItemStacks.head(REGISTER_TEXTURE, String.join("\n", upgradeLines((SkyBlockPlayer) c.player()))),
                (_, c) -> {
                    buy((SkyBlockPlayer) c.player());
                    c.session(DefaultState.class).refresh();
                });
        layout.slot(15, (_, _) -> ItemStacks.head(BAG_TEXTURE, """
                <a>Accessory Bag Shortcut
                <7>Quickly access your accessory
                <7>bag from right here!

                <e>Click to open!"""), (_, c) -> c.push(new GUIAccessoryBag()));
    }

    public static long costOfUpgrade(int upgradeNumber) {
        if (upgradeNumber <= 1) return 1_500_000L;
        if (upgradeNumber <= 5) return 5_000_000L;
        if (upgradeNumber <= 10) return 8_000_000L;
        if (upgradeNumber <= 20) return 12_000_000L;
        return 20_000_000L;
    }

    private static int upgrades(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.JACOBUS_UPGRADES, DatapointInteger.class).getValue();
    }

    private static List<String> upgradeLines(SkyBlockPlayer player) {
        int upgrades = upgrades(player);
        List<String> lines = new ArrayList<>();
        lines.add("<a>Accessory Bag Upgrades");
        lines.add("<7>Increase the size of your");
        lines.add("<7>accessory bag.");
        lines.add("");
        lines.add("<7>Redstone collection: <c>" + GUIAccessoryBag.getCollectionSlots(player) + " Slots");
        lines.add("<7>Jacobus: <6>+" + GUIAccessoryBag.getJacobusSlots(player) + " Slots");
        lines.add("");
        if (upgrades >= MAX_UPGRADES) {
            lines.add("<a>Maxed out!");
            return lines;
        }
        long cost = costOfUpgrade(upgrades + 1);
        lines.add("<7>Buying: <6>+" + SLOTS_PER_UPGRADE + " Slots");
        lines.add("<7>Cost: <6>" + StringUtility.commaify(cost) + " Coins");
        lines.add("");
        lines.add(player.getCoins() >= cost ? "<e>Click to upgrade!" : "<c>Can't afford upgrade!");
        return lines;
    }

    private static void buy(SkyBlockPlayer player) {
        int upgrades = upgrades(player);
        if (upgrades >= MAX_UPGRADES) {
            player.sendMessage("<c>You have reached the maximum amount of Jacobus upgrades! That was NOT a smart business decision.");
            return;
        }
        if (GUIAccessoryBag.getCollectionSlots(player) <= 0) {
            player.sendMessage("<c>You need to unlock your Accessory Bag first!");
            return;
        }
        long cost = costOfUpgrade(upgrades + 1);
        if (player.getCoins() < cost) {
            player.sendMessage("<c>You can't afford this upgrade!");
            return;
        }
        player.removeCoins(cost);
        player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.JACOBUS_UPGRADES, DatapointInteger.class).setValue(upgrades + 1);
        player.getSkyBlockExperience().addSourceXP(SkyBlockXPSource.ACCESSORY_BAG_UPGRADES, XP_PER_UPGRADE);
        player.sendMessage("<a>You upgraded your Accessory Bag by <6>+" + SLOTS_PER_UPGRADE + " Slots<a>!");
        if (upgrades + 1 == REGISTER_AT_UPGRADES) {
            player.addAndUpdateItem(new SkyBlockItem(ItemType.JACOBUS_REGISTER));
            player.sendMessage("<a>Jacobus gave you a <6>Jacobus Register<a>!");
        }
    }
}
