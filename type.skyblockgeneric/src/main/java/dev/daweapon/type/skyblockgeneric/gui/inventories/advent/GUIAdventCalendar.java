package dev.daweapon.type.skyblockgeneric.gui.inventories.advent;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.advent.AdventCalendar;
import dev.daweapon.type.skyblockgeneric.advent.AdventService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;

import java.util.Set;

public class GUIAdventCalendar extends StatelessView {
    private static final int[] SLOTS = {
            2, 3, 4, 5, 6,
            11, 12, 13, 14, 15,
            20, 21, 22, 23, 24,
            29, 30, 31, 32, 33,
            38, 39, 40, 41, 42};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Advent Calendar", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        Components.fill(layout);
        Components.close(layout, 49);
        int open = AdventService.openDay(player);
        for (int number = 1; number <= AdventCalendar.DAYS; number++) {
            int day = number;
            layout.slot(SLOTS[number - 1], (s, c) -> render(player, day, open), (click, c) -> {
                AdventService.claim((SkyBlockPlayer) c.player(), day);
                c.session(DefaultState.class).refresh();
            });
        }
    }

    private static net.minestom.server.item.ItemStack.Builder render(SkyBlockPlayer player, int number, int open) {
        Set<Integer> claimed = AdventService.claimed(player);
        AdventCalendar.Day day = AdventCalendar.day(number);
        StringBuilder lore = new StringBuilder();
        day.gifts().forEach((gift, amount) -> lore.append("<7>").append(amount).append("x <f>").append(StringUtility.toNormalCase(gift)).append("\n"));
        lore.append("<7>").append(StringUtility.commaify(day.coins())).append(" <6>coins\n");
        for (AdventCalendar.RankedItem item : day.items()) {
            lore.append(player.getRank().isEqualOrHigherThan(item.rank()) ? "<a>" : "<c>").append("1x ").append(StringUtility.toNormalCase(item.item()))
                    .append(" <8>(").append(item.rank().getTitle()).append(")\n");
        }
        day.unbuilt().forEach(name -> lore.append("<8>").append(name).append(" (not given yet)\n"));
        if (claimed.contains(number)) {
            return ItemStacks.item(Material.LIME_STAINED_GLASS_PANE, "<a>Day " + number + "\n" + lore + "\n<a>Already claimed!");
        }
        if (number > open) {
            return ItemStacks.item(Material.RED_STAINED_GLASS_PANE, "<c>Day " + number + "\n" + lore + "\n<c>Not available yet!");
        }
        return ItemStacks.item(Material.CHEST, "<e>Day " + number + "\n" + lore + "\n<e>Click to claim!");
    }
}
