package dev.daweapon.type.skyblockgeneric.century.raffle;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.View;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.updater.NonPlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.List;

public class GUIRaffleRewards implements View<GUIRaffleRewards.State> {
    private static final int[] SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43};
    private static final int PREVIOUS_SLOT = 45;
    private static final int BACK_SLOT = 48;
    private static final int CLOSE_SLOT = 49;
    private static final int NEXT_SLOT = 53;

    public record State(RaffleType type, String label, int page) {
        State withPage(int page) {
            return new State(type, label, page);
        }
    }

    private record Prize(String id, PrizeTier tier) {
    }

    public static void open(SkyBlockPlayer player, RaffleType type, String label) {
        player.openView(new GUIRaffleRewards(), new State(type, label, 0));
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.withString((state, _) -> state.label() + " ➜ Rewards", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        Components.fill(layout);
        List<Prize> prizes = prizes();
        int pages = Math.max(1, (prizes.size() + SLOTS.length - 1) / SLOTS.length);
        int page = Math.min(state.page(), pages - 1);
        CenturyRaffleConfig config = CenturyRaffleConfig.get();
        for (int i = 0; i < SLOTS.length; i++) {
            int index = page * SLOTS.length + i;
            if (index >= prizes.size()) break;
            Prize prize = prizes.get(index);
            long winners = config.prizeCount(state.type(), prize.tier());
            int poolSize = config.pool(prize.tier()).size();
            layout.slot(SLOTS[i], (_, _) -> ItemStacks.appendLore(icon(prize.id()), List.of(
                    Text.empty(),
                    Text.of(prize.tier().display() + " <r><7>reward"),
                    Text.of("<7>" + StringUtility.commaify(winners) + " " + prize.tier().display()
                            + " <r><7>rewards per draw, each one"),
                    Text.of("<7>of these <a>" + poolSize + " <7>prizes."))));
        }
        Components.prevPage(layout, PREVIOUS_SLOT, State::page, State::withPage);
        Components.nextPage(layout, NEXT_SLOT, State::page, _ -> pages, State::withPage);
        layout.slot(BACK_SLOT, Components.BACK_BUTTON, (c, _) -> RaffleMenus.openBox((SkyBlockPlayer) c.player()));
        Components.close(layout, CLOSE_SLOT);
    }

    private static List<Prize> prizes() {
        List<Prize> prizes = new ArrayList<>();
        for (PrizeTier tier : PrizeTier.values()) {
            for (String id : CenturyRaffleConfig.get().pool(tier)) prizes.add(new Prize(id, tier));
        }
        return prizes;
    }

    static ItemStack.Builder icon(String prize) {
        return switch (prize) {
            case CenturyRaffleConfig.RANDOM_CENTURY_CAKE -> ItemStacks.item(Material.CAKE, """
                    <a>Random Century Cake
                    <7>One of the <a>20 <7>Century Cakes.""");
            case CenturyRaffleConfig.RANDOM_CAKE_SOUL -> ItemStacks.item(Material.PINK_DYE, """
                    <d>Random Cake Soul
                    <7>A Cake Soul of a random colour.""");
            default -> {
                ItemType type = ItemType.get(prize);
                yield type == null ? ItemStacks.item(Material.BARRIER, "<c>" + prize)
                        : new NonPlayerItemUpdater(new SkyBlockItem(type)).getUpdatedItem();
            }
        };
    }
}
