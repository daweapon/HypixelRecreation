package dev.daweapon.type.skyblockgeneric.gui.inventories.starborn;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.View;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.torrhus.StarbornBenefactors;
import dev.daweapon.type.skyblockgeneric.torrhus.StarbornTemple;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.Material;

import java.util.List;

public class GUIStarbornAltar implements View<GUIStarbornAltar.State> {
    private static final int[] DONATION_SLOTS = {10, 12, 14, 16};

    public record State(StarbornTemple temple) {
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.withString((state, context) -> state.temple().displayName() + " Temple Altar", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext context) {
        Components.fill(layout);
        StarbornTemple temple = state.temple();
        layout.slot(4, (s, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            long left = StarbornBenefactors.millisLeft(player, temple);
            return ItemStacks.item(Material.NETHER_STAR, """
                            <d>Will
                            <7>Benefactors: <9>{}
                            <7>Hunter Fortune: <a>{}

                            <7>Benefactor: {}
                            <7>Your Black Holes and Huntraps work <a>25% <7>faster
                            <7>and the <d>Will <7>bonus is doubled.""",
                    StringUtility.commaify(StarbornBenefactors.count(temple)),
                    "%.2f".formatted(StarbornBenefactors.will(player, temple)),
                    left > 0 ? StringUtility.formatTimeLeft(left) : "Inactive");
        });
        List<Integer> days = StarbornTemple.DONATION_DAYS;
        for (int i = 0; i < DONATION_SLOTS.length; i++) {
            int duration = days.get(i);
            layout.slot(DONATION_SLOTS[i], (s, c) -> ItemStacks.item(Material.GOLD_INGOT, duration, """
                            <9>Donate for {} Day{}
                            <7>Cost: <6>{} Coins <7>or <a>{}x {}

                            <e>Left-click to donate items!
                            <e>Right-click to donate coins!""",
                            duration, duration == 1 ? "" : "s", StringUtility.commaify(StarbornTemple.coinCost(duration)),
                            StarbornTemple.itemCost(duration), temple.donationItem().getDisplayName()),
                    (click, c) -> {
                        boolean coins = click.click() instanceof Click.Right || click.click() instanceof Click.RightShift;
                        SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                        if (StarbornBenefactors.donate(player, temple, duration, coins) == StarbornBenefactors.Result.CANNOT_AFFORD) {
                            player.sendMessage("<c>You cannot afford that donation!");
                        }
                        player.closeInventory();
                    });
        }
        Components.close(layout, 31);
    }

    public static void open(SkyBlockPlayer player, StarbornTemple temple) {
        player.openView(new GUIStarbornAltar(), new State(temple));
    }
}
