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
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("SkyBlock Advent Calendar 2022", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        Components.fill(layout);
        Components.back(layout, 48, ctx);
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

    private static String ordinal(int number) {
        if (number % 100 >= 11 && number % 100 <= 13) return number + "th";
        return number + switch (number % 10) {
            case 1 -> "st";
            case 2 -> "nd";
            case 3 -> "rd";
            default -> "th";
        };
    }

    private static net.minestom.server.item.ItemStack.Builder render(SkyBlockPlayer player, int number, int open) {
        Set<Integer> claimed = AdventService.claimed(player);
        AdventCalendar.Day day = AdventCalendar.day(number);
        boolean done = claimed.contains(number);
        String mark = done ? "<a>✔ " : "<c>✘ ";
        StringBuilder lore = new StringBuilder("<8>Advent Reward\n\n<7>Rewards:\n");
        day.gifts().forEach((gift, amount) -> lore.append(mark).append("<8>+<f>").append(amount).append("x ").append(StringUtility.toNormalCase(gift)).append("\n"));
        lore.append(mark).append("<8>+<6>").append(StringUtility.commaify(day.coins())).append(" <7>Coins\n");
        for (AdventCalendar.RankedItem item : day.items()) {
            lore.append(player.getRank().isEqualOrHigherThan(item.rank()) ? mark : "<c>✘ ").append("<8>+<f>1x ").append(StringUtility.toNormalCase(item.item()))
                    .append(" <8>(").append(item.rank().getTitle()).append(")\n");
        }
        for (AdventCalendar.RankedSpray spray : day.sprays()) {
            lore.append(player.getRank().isEqualOrHigherThan(spray.rank()) ? mark : "<c>✘ ").append("<8>+<d>").append(StringUtility.toNormalCase(spray.spray()))
                    .append(" <7>Spray <8>(").append(spray.rank().getTitle()).append(")\n");
        }
        day.unbuilt().forEach(name -> lore.append("<8>").append(name).append(" (not given yet)\n"));
        String name = "December " + ordinal(number);
        if (done) {
            return ItemStacks.item(Material.LIME_STAINED_GLASS_PANE, "<a><bold>" + name + "\n" + lore + "\n<a>Reward claimed!");
        }
        if (number > open) {
            return ItemStacks.item(Material.RED_STAINED_GLASS_PANE, "<c><bold>" + name + "\n" + lore + "\n<c>Not available yet!");
        }
        return ItemStacks.item(Material.CHEST, "<e><bold>" + name + "\n" + lore + "\n<e>Click to claim reward!");
    }
}
